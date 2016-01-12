import com.google.protobuf.ByteString

import play.api.libs.json._

import scala.util.control.NonFatal

sealed abstract class Raw extends Product with Serializable {
  def toJson: JsValue
  override def toString = Json.prettyPrint(toJson)
}

final case class Varint(value: Long) extends Raw {
  def toJson = Json.obj("variant" -> value)
}
final case class Number64(value: Long) extends Raw{
  def toJson = Json.obj("number64" -> value)
}
final case class Number32(value: Int) extends Raw{
  def toJson = Json.obj("number32" -> value)
}
final case class Length(value: ByteString) extends Raw {
  def toJson = Json.obj(
    "size" -> value.size,
    "contents" -> Json.obj(
      ("raw bytes", value.toByteArray.map(_.toHexString).mkString(" ")),
      ("if string", value.toStringUtf8),
      ("if embed", try{
        Fields.fromBytes(value.toByteArray) match {
          case Right(fields) =>
            fields.toJson
          case Left(e) =>
            Json.obj("error" -> e.toString)
        }
      }catch{
        case NonFatal(e) =>
          Json.obj("error" -> e.toString)
      })
    )
  )

}
