import java.io.ByteArrayOutputStream

import com.google.protobuf.{CodedInputStream, WireFormat, CodedOutputStream}
import play.api.libs.json.{Json, JsArray, JsValue}

import scala.annotation.tailrec

final case class Fields(values: List[Field]) {
  def toJson: JsValue = JsArray(values.map(_.toJson))
  override def toString = Json.prettyPrint(toJson)
}

object Fields {
  def fieldsToBytes(values: Seq[Field]): Array[Byte] = {
    val buf = new ByteArrayOutputStream()
    val out = CodedOutputStream.newInstance(buf)
    @tailrec
    def loop(xs: List[Field]): Unit = xs match {
      case h :: t =>
        h.value match {
          case Varint(a) =>
            out.writeInt64(h.fieldNumber, a)
          case Number64(a) =>
            out.writeFixed64(h.fieldNumber, a)
          case Number32(a) =>
            out.writeFixed32(h.fieldNumber, a)
          case Length(a) =>
            out.writeTag(h.fieldNumber, WireFormat.WIRETYPE_LENGTH_DELIMITED)
            out.writeRawVarint32(a.size())
            out.writeRawBytes(a)
        }
        loop(t)
      case Nil =>
    }
    loop(values.toList)
    out.flush()
    buf.toByteArray
  }

  private[this] def wireTypeString(i: Int) = i match {
    case WireFormat.WIRETYPE_VARINT =>
      "VARINT(000)"
    case WireFormat.WIRETYPE_FIXED64 =>
      "FIX_64(001)"
    case WireFormat.WIRETYPE_FIXED32 =>
      "FIX_32(101)"
    case WireFormat.WIRETYPE_LENGTH_DELIMITED =>
      "LENGTH(010)"
    case WireFormat.WIRETYPE_START_GROUP =>
      "STARTG(011)"
    case WireFormat.WIRETYPE_END_GROUP =>
      "END__G(100)"
  }

  def main(args: Array[String]): Unit = {
    val bytes = "0a 08 4f 75 72 20 6d 65 6e 75 12 0f 0a 04 61 61 61 61 12 04 62 62 62 62 18 85 1a".split(" ").map(Integer.parseInt(_, 16).toByte)
    println(new String(bytes))
    println("\n")
    fromBytes(bytes) match {
      case Right(a) =>
        println("parse success\n")
        println(a)
      case Left(e) =>
        println("parse fail " + e)
    }
  }

  def fromBytes(bytes: Array[Byte]): Either[String, Fields] = {
    val in = CodedInputStream.newInstance(bytes)

    @tailrec def loop(acc: List[Field]): Either[String, List[Field]] = in.readTag() match {
      case 0 =>
        Right(acc.reverse)
      case tag =>
        val w = WireFormat.getTagWireType(tag)

        def b(i: Int) = Bit(i.toByte)
        val fieldNum = List(b(tag >> 24), b(tag >> 16), b(tag >> 8), b(tag).tagString).mkString(" ")
        val fieldNum2 = List(b(tag >> (24 + 3)), b(tag >> (16 + 3)), b(tag >> (8 + 3)), b(tag >> 3)).mkString(" ")

        //println(s"${fieldNum} ${wireTypeString(w)}")

        //println(fieldNum2 + " " + WireFormat.getTagFieldNumber(tag))

        val value = WireFormat.getTagWireType(tag) match {
          case WireFormat.WIRETYPE_VARINT =>
            Right(Varint(in.readRawVarint64()))
          case WireFormat.WIRETYPE_FIXED64 =>
            Right(Number64(in.readRawLittleEndian64()))
          case WireFormat.WIRETYPE_FIXED32 =>
            Right(Number32(in.readRawLittleEndian32()))
          case WireFormat.WIRETYPE_LENGTH_DELIMITED =>
            Right(Length(in.readBytes()))
          case WireFormat.WIRETYPE_START_GROUP =>
            val message = "could not parse start group "
            Left(message + acc.reverse)
          case WireFormat.WIRETYPE_END_GROUP =>
            val message = "could not parse end group "
            Left(message + acc.reverse)
          case other =>
            Left("invalid tag " + other)
        }


        value match {
          case Right(v) =>
            val f = Field(WireFormat.getTagFieldNumber(tag), v)
            loop(f :: acc)
          case Left(e) =>
            Left(e)
        }
    }

    loop(Nil).right.map(Fields.apply)
  }

}
