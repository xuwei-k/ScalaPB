package com.trueaccord.scalapb.compiler

import java.io.File
import java.nio.file.Files

import com.github.os72.protocjar.Protoc
import org.scalatest.FunSpec

import scala.reflect.ClassTag
import scala.tools.nsc.reporters.ConsoleReporter

object Test{
  private case class SourceCode(name: String, contents: String)

  private def compileScala(sourceCode: List[SourceCode]): Boolean = sbt.IO.withTemporaryDirectory{ dir =>
    def jarForClass[T](implicit c: ClassTag[T]) =
      c.runtimeClass.getProtectionDomain.getCodeSource.getLocation.getPath

    val classPath = Seq(
      jarForClass[annotation.Annotation],
      jarForClass[com.trueaccord.scalapb.GeneratedMessage],
      jarForClass[com.trueaccord.scalapb.Scalapb],
      jarForClass[com.google.protobuf.Message],
      jarForClass[io.grpc.Channel],
      jarForClass[com.trueaccord.lenses.Lens[_, _]],
      dir
    )
    import scala.tools.nsc.{Settings, Global}

    val s = new Settings(error => throw new RuntimeException(error))
    s.processArgumentString( s"""-cp "${classPath.mkString(":")}" -d "$dir"""")
    val reporter = new ConsoleReporter(s)
    val g = new Global(s, reporter)

    import sbt.Path._
    sourceCode.foreach{ src =>
      sbt.IO.write(dir / src.name, src.contents)
    }
    val run = new g.Run
    run.compile(sourceCode.map(src => (dir / src.name).getAbsolutePath))
    reporter.hasErrors == false
  }

}

class Test extends FunSpec {

  describe("rpc") {
    import sbt.Path._

    val protoDirs = List(
      "protobuf/scalapb/scalapb.proto",
      "third_party/google/protobuf/compiler/",
      "third_party/google/protobuf/"
    ).map(new File(_).getAbsolutePath)

    it("test") {
      sbt.IO.withTemporaryDirectory{ inputDir =>
        sbt.IO.withTemporaryDirectory{ outDir =>
          val inputProto: File = inputDir / "sample1.proto"
          val packageName = "com.example"
          val input = s"""
          syntax = "proto3";

          package $packageName;

          message Req1 {}
          message Res1 {}

          message Req2 {}
          message Res2 {}

          service Service1 {
            rpc hello (Req1) returns (Res1) {}
            rpc foo (Req2) returns (Res2) {}
          }
          """
          Files.write(inputProto.toPath, java.util.Collections.singletonList(input))
          val args: Array[String] = (inputDir :: protoDirs).map("-I" + _).toArray ++ Array[String](
            "--scala_out=" + outDir.getAbsolutePath,
            inputProto.getAbsolutePath
          )

          val res = ProtocDriverFactory.create().buildRunner { a => Protoc.runProtoc("-v300" +: a.toArray) }(args)
          assert(res == 0)
          val files = (outDir ** "*.scala").get
          println("file count " + files.size)
          println(files.map(_.getName))
//          files.foreach(f => println(sbt.IO.read(f) + "\n" + ("-" * 200) + "\n"))
          val sources = files.map{ f =>
            Test.SourceCode(f.getName, sbt.IO.read(f))
          }

          assert(Test.compileScala(sources.toList))
        }
      }

    }
  }
}
