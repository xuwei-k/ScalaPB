import com.google.protobuf.ByteString

sealed abstract class Raw extends Product with Serializable

final case class Varint(value: Long) extends Raw
final case class Number64(value: Long) extends Raw
final case class Number32(value: Int) extends Raw
final case class Length(value: ByteString) extends Raw
