package com.trueaccord.scalapb.compiler

import java.io.{InputStream, StringWriter, PrintWriter}
import java.net.ServerSocket
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.{Files, Path}

import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorResponse, CodeGeneratorRequest}
import com.trueaccord.scalapb.Scalapb

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process._
import scala.util.Try

/** A ProtocDriver instance provides a platform-dependent way to launch protoc and
  * and set up a two-way communication channel between protoc and this JVM.
  *
  * protoc is able to launch plugins. A plugin is expected to take a serialized
  * CodeGenerationRequest via stdin and serialize a CodeGenerationRequest to stdout.
  * The idea in ProtocDriver is to create a minimal plugin that wires its stdin/stdout
  * to this JVM.
  *
  * The two-way communication always goes as follows:
  *
  * 1. protoc writes a request to the stdin of a plugin
  * 2. plugin writes the data to the channel
  * 3. this JVM reads it, interprets it as CodeGenerationRequest and process it.
  * 4. this JVM writes a CodeGenerationResponse to the channel
  * 5. this JVM closes the channel.
  * 6. the plugin reads the data and writes it to standard out.
  * 7. protoc handles the CodeGenerationResponse (creates Scala sources)
  */
trait ProtocDriver {
  def buildRunner[A](runner: Seq[String] => A)(params: Seq[String]): A
}

object ProtocDriverFactory {
  private def isWindows: Boolean = sys.props("os.name").startsWith("Windows")

  def create() =
    if (isWindows) new WindowsProtocDriver("python.exe")
    else new PosixProtocDriver
}

/** A driver that creates a named pipe and sets up a shell script as a protoc plugin */
class PosixProtocDriver extends ProtocDriver {
  def buildRunner[A](runner: Seq[String] => A)(params: Seq[String]): A = {
    val pipe = createPipe()
    val sh = createShellScript(pipe)
    Future {
      val fsout = Files.newOutputStream(pipe)
      val fsin = Files.newInputStream(pipe)
      try {
        val response = Process.runWithInputStream(fsin)
        fsout.write(response.toByteArray)
        fsout.close()
      } catch {
        case e: Throwable =>
          val res = CodeGeneratorResponse.newBuilder()
            .setError(e.toString + "\n" + Process.getStackTrace(e))
            .build
          fsout.write(res.toByteArray)
          fsout.close()
      } finally {
        fsin.close()
      }
    }

    val args = Seq(s"--plugin=protoc-gen-scala=$sh") ++ params
    runner(args)
  }

  private def createPipe(): Path = {
    val pipeName = Files.createTempFile("protopipe-", ".pipe")
    Files.delete(pipeName)
    Seq("mkfifo", "-m", "600", pipeName.toAbsolutePath.toString).!!
    pipeName
  }

  private def createShellScript(tmpFile: Path): Path = {
    val scriptName = Process.createTempFile("",
      s"""|#!/usr/bin/env sh
          |set -e
          |cat /dev/stdin > "$tmpFile"
          |cat "$tmpFile"
      """.stripMargin)
    Files.setPosixFilePermissions(scriptName, Set(
      PosixFilePermission.OWNER_EXECUTE,
      PosixFilePermission.OWNER_READ))
    scriptName
  }
}

  /** A driver that binds a server socket to a local interface. The plugin
    * is a batch script that invokes Python, which will communicate wire its
    * stdin and stdout to this socket.
    */
class WindowsProtocDriver(pythonExecutable: String) extends ProtocDriver {
  def buildRunner[A](runner: Seq[String] => A)(params: Seq[String]): A = {
    val ss = new ServerSocket(0)
    val (batFile, pyFile) = createWindowsScripts(ss.getLocalPort)
    Future {
      val client = ss.accept()
      val response = Process.runWithInputStream(client.getInputStream)
      client.getOutputStream.write(response.toByteArray)
      client.close()
      ss.close()
    }

    try {
      val args = Seq(s"--plugin=protoc-gen-scala=$batFile") ++ params
      runner(args)
    } finally {
      Files.delete(batFile)
      Files.delete(pyFile)
    }
  }

  private def createWindowsScripts(port: Int): (Path, Path) = {
    val pythonScript = Process.createTempFile(".py",
      s"""|import sys, socket
         |
         |content = sys.stdin.read()
         |s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
         |s.connect(('127.0.0.1', int(sys.argv[1])))
         |s.sendall(content)
         |s.shutdown(socket.SHUT_WR)
         |while 1:
         |    data = s.recv(1024)
         |    if data == '':
         |        break
         |    sys.stdout.write(data)
         |s.close()
      """.stripMargin)

    val batchFile = Process.createTempFile(".bat",
      s"""@echo off
         |$pythonExecutable -u $pythonScript $port
         """.stripMargin)
    (batchFile, pythonScript)
  }
}

object Process {
  def getStackTrace(e: Throwable): String = {
    val stringWriter = new StringWriter
    val printWriter = new PrintWriter(stringWriter)
    e.printStackTrace(printWriter)
    stringWriter.toString
  }

  def runWithInputStream(fsin: InputStream): CodeGeneratorResponse = {
        CodeGeneratorResponse.newBuilder()
          .setError("e" * 200)
          .build

/*
    try {
      val registry = ExtensionRegistry.newInstance()
      Scalapb.registerAllExtensions(registry)
      val request = CodeGeneratorRequest.parseFrom(fsin, registry)
      ProtobufGenerator.handleCodeGeneratorRequest(request)
    } catch {
      case throwable: Throwable =>
        CodeGeneratorResponse.newBuilder()
          .setError(throwable.toString + "\n" + getStackTrace(throwable))
          .build
    }
    */
  }

  def createTempFile(extension: String, content: String): Path = {
    val fileName = Files.createTempFile("scalapbgen", extension)
    val os = Files.newOutputStream(fileName)
    os.write(content.getBytes("UTF-8"))
    os.close()
    fileName
  }
}
