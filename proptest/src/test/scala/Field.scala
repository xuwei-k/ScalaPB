import play.api.libs.json.{Json, JsValue}

final case class Field(fieldNumber: Int, value: Raw){
  def toJson: JsValue = Json.obj(
      "number" -> fieldNumber,
      "value" -> value.toJson
  )
  override def toString = Json.prettyPrint(toJson)
}
