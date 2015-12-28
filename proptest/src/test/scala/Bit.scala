final case class Bit(byte: Byte) {
  override def toString: String = toColorStr()

  def tagString: String = toColorStr(
    _0 = Color.Blue,
    _1 = Color.Blue,
    _2 = Color.Blue
  )

  def toColorStr(
    _0: Color = Color.Normal,
    _1: Color = Color.Normal,
    _2: Color = Color.Normal,
    _3: Color = Color.Normal,
    _4: Color = Color.Normal,
    _5: Color = Color.Normal,
    _6: Color = Color.Normal,
    _7: Color = Color.Normal
  ): String = {

    List(_0, _1, _2, _3, _4, _5, _6, _7).zipWithIndex.reverse.map{
      case (c, i) =>
        c.wrap(if((byte >> i & 1) == 1) "1" else "0")
    }.mkString
  }
}
