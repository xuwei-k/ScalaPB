sealed abstract class Color(x: String) extends Product with Serializable {
  def wrap(string: String): String = {
    Console.RESET + x + string + Console.RESET
  }
}
object Color{
  case object Normal extends Color("")
  case object Red extends Color(Console.RED)
  case object Blue extends Color(Console.BLUE)
  case object Green extends Color(Console.GREEN)
}
