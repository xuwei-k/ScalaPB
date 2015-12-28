
import SchemaGenerators.CompiledSchema
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
    forAll(SchemaGenerators.genCompiledSchema, workers(1), minSuccessful(20)) {
      schema: CompiledSchema =>
        forAll(GenData.genMessageValueInstance(schema.rootNode)) {
          case (message, messageValue) =>
            // Ascii to binary is the same.
            val messageAscii = messageValue.toAscii
            val companion = schema.scalaObject(message)
            val scalaProto = companion.fromAscii(messageValue.toAscii)
            val scalaBytes = scalaProto.toByteArray

            val x = Fields.fromBytes(scalaBytes)
            assert(ByteArray(Fields.fieldsToBytes(x)) === ByteArray(scalaBytes), scalaProto + " " + message)
        }
    }
  }
}
