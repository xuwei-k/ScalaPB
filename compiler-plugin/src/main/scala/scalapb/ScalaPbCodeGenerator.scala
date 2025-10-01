package scalapb

import com.google.protobuf.DescriptorProtos.Edition
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import scalapb.compiler.ProtobufGenerator
import scalapb.options.Scalapb
import com.google.protobuf.{CodedInputStream, ExtensionRegistry, UnknownFieldSet}
import protocbridge.{Artifact, ProtocCodeGenerator}
import protocgen.CodeGenRequest

import scala.jdk.CollectionConverters.asJavaIterableConverter

object ScalaPbCodeGenerator extends ProtocCodeGenerator {
  def registerExtensions(registry: ExtensionRegistry): Unit =
    Scalapb.registerAllExtensions(registry)

  final override def run(req: Array[Byte]): Array[Byte] =
    run(CodedInputStream.newInstance(req))

  private def errorMessage(t: Throwable) = {
    val sw = new java.io.StringWriter()
    t.printStackTrace(new java.io.PrintWriter(sw, true))
    sw.toString
  }

  final def run(input: CodedInputStream): Array[Byte] = {
    try {
      val registry = ExtensionRegistry.newInstance()
      registerExtensions(registry)
      val request = CodeGenRequest(
        CodeGeneratorRequest.parseFrom(input, registry)
      )

      val res = process(request) match {
        case Right((files, features)) =>
          CodeGeneratorResponse
            .newBuilder()
            .addAllFile(files.asJava)
            .setSupportedFeatures(features.map(_.getNumber()).sum.toLong)
            .setUnknownFields(
              // https://github.com/protocolbuffers/protobuf/blob/1f60d67437d7f57700/src/google/protobuf/compiler/plugin.proto#L105-L115
              UnknownFieldSet
                .newBuilder()
                .addField(
                  3,
                  UnknownFieldSet.Field
                    .newBuilder()
                    .addVarint(
                      0
                    )
                    .build()
                )
                .addField(
                  4,
                  UnknownFieldSet.Field
                    .newBuilder()
                    .addVarint(
                      Edition.EDITION_2024.getNumber.toLong
                    )
                    .build()
                )
                .build()
            )
            .build()
        case Left(msg) =>
          val b = CodeGeneratorResponse.newBuilder()
          b.setError(msg)
          b.build()
      }
      res.toByteArray()
    } catch {
      case t: Throwable =>
        CodeGeneratorResponse
          .newBuilder()
          .setError(errorMessage(t))
          .build()
          .toByteArray
    }
  }

  def process(request: CodeGenRequest) =
    ProtobufGenerator.handleCodeGeneratorRequest(request)

  override def suggestedDependencies: Seq[Artifact] = Seq(
    Artifact(
      "com.thesamet.scalapb",
      "scalapb-runtime",
      scalapb.compiler.Version.scalapbVersion,
      crossVersion = true
    )
  )
}
