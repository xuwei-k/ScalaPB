package com.trueaccord.scalapb.compiler

import java.io.File
import java.nio.file.Files

import com.github.os72.protocjar.Protoc
import org.scalatest.FunSpec

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
          val inputProto: File = inputDir / "sample.proto"
          val packageName = "sample"
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

          ProtocDriverFactory.create().buildRunner { a => Protoc.runProtoc("-v300" +: a.toArray) }(args)
          println(outDir.listFiles().length)
          (outDir ** "*.scala").get.foreach{ f =>
//            println(f)
//            sbt.IO.readLines(f).foreach(println)
 //           println
          }
        }
      }

    }
  }
}
