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
      jarForClass[javax.annotation.Nullable],
      jarForClass[com.trueaccord.scalapb.GeneratedMessage],
      jarForClass[com.trueaccord.scalapb.Scalapb],
      jarForClass[com.google.protobuf.Message],
      jarForClass[com.google.common.util.concurrent.ListenableFuture[_]],
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
      "third_party/",
      "protobuf/"
    ).map(new File(_).getAbsolutePath)

    it("test") {
      sbt.IO.withTemporaryDirectory{ inputDir =>
        sbt.IO.withTemporaryDirectory{ outDir =>
          val protoFileName = "route_guide.proto"
          val inputProto: File = inputDir / protoFileName
          val input = sbt.IO.readStream(this.getClass.getResourceAsStream("/" + protoFileName))
          Files.write(inputProto.toPath, java.util.Collections.singletonList(input))
          val args: Array[String] = (inputDir :: protoDirs).map("-I" + _).toArray ++ Array[String](
            "--scala_out=java_conversions:" + outDir.getAbsolutePath,
            "--java_out=" + outDir.getAbsolutePath,
            inputProto.getAbsolutePath
          )

          val res = ProtocDriverFactory.create().buildRunner { a => Protoc.runProtoc("-v300" +: a.toArray) }(args)
          assert(res == 0)
          val files = ((outDir ** "*.scala") +++ (outDir ** "*.java")).get
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
