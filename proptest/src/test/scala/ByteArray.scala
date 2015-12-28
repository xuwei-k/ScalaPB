final case class ByteArray(value: Array[Byte]){
  override def toString =
    s"""ByteArray(size = ${value.length},
       |${value.map(Bit(_)).mkString("", " ", "")}
       |${value.map("%02x" format _).map{x => s"   ${x.head}   ${x.last}"}.mkString("", " ", "")}
       |""".stripMargin
  override def equals(x: scala.Any) = x match {
    case that: ByteArray => java.util.Arrays.equals(value, that.value)
    case _ => false
  }
}

