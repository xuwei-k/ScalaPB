import java.io.{StringReader, File, PrintWriter}
import java.util.logging.LogManager

import SchemaGenerators.CompiledSchema
import com.google.protobuf
import com.google.protobuf.TextFormat
import com.trueaccord.scalapb.{GeneratedMessage, JavaProtoSupport}
import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import scala.language.existentials

class GeneratedCodeSpec extends PropSpec with GeneratorDrivenPropertyChecks with Matchers {
  property("min and max id are consecutive over files") {
    forAll(GraphGen.genRootNode) {
      node =>
        def validateMinMax(pairs: Seq[(Int, Int)]) =
          pairs.sliding(2).filter(_.size == 2).forall {
            case Seq((min1, max1), (min2, max2)) => min2 == max1 + 1 && min2 <= max2
          }
        val messageIdPairs: Seq[(Int, Int)] = node.files.flatMap { f => (f.minMessageId.map((_, f.maxMessageId.get)))}
        val enumIdPairs: Seq[(Int, Int)] = node.files.flatMap { f => (f.minEnumId.map((_, f.maxEnumId.get)))}
        validateMinMax(messageIdPairs) && validateMinMax(enumIdPairs)
    }
  }

  property("Java and Scala protos are equivalent") {
    LogManager.getLogManager().reset()
    forAll(SchemaGenerators.genCompiledSchema, workers(1), minSuccessful(20)) {
      schema: CompiledSchema =>
        forAll(GenData.genMessageValueInstance(schema.rootNode)) {
          case (message, messageValue) =>
            // Ascii to binary is the same.
            val messageAscii = messageValue.toAscii
            try {
              val builder = schema.javaBuilder(message)
              TextFormat.merge(messageAscii, builder)
              val javaProto: protobuf.Message = builder.build()
              val companion = schema.scalaObject(message)
              val scalaProto = companion.fromAscii(messageValue.toAscii)
              val scalaBytes = scalaProto.toByteArray

              // Parsing in Scala the serialized bytes should give the same object.
              val scalaParsedFromBytes = companion.parseFrom(scalaBytes)
              scalaParsedFromBytes.toString should be(scalaProto.toString)
              scalaParsedFromBytes should be(scalaProto)

              // Parsing in Java the bytes serialized by Scala should give back javaProto:
              val javaProto2 = schema.javaParse(message, scalaBytes)
              javaProto2 should be(javaProto)

              // getAllFields and fromFieldsMap should return the same object
              companion.fromFieldsMap(scalaProto.getAllFields) should be(scalaProto)

              // toJavaProto, fromJava should bring back the same object.
              val javaConversions = companion.asInstanceOf[JavaProtoSupport[GeneratedMessage, protobuf.Message]]
              javaConversions.fromJavaProto(javaProto) should be (scalaProto)
              javaConversions.toJavaProto(scalaProto) should be (javaProto)

              companion.fromJsonString(scalaProto.toJsonString) should be(scalaProto)
              companion.fromJsonReader(new StringReader(scalaProto.toJsonString)) should be(scalaProto)
            } catch {
              case e: Exception =>
                println(e.printStackTrace)
                println("Message: " + message.name)
                val pw = new PrintWriter(new File("/tmp/message.ascii"))
                pw.print(messageAscii)
                pw.close()
                throw new RuntimeException(e.getMessage, e)
            }
        }
    }
  }
}
