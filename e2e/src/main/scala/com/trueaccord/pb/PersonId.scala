package com.trueaccord.pb

import com.trueaccord.scalapb.TypeMapper
import com.trueaccord.proto.e2e.custom_types.CustomMessage.Name
import com.trueaccord.proto.e2e.custom_types.CustomMessage.Weather
import org.scalacheck.{Gen, Arbitrary}

case class PersonId(untypedId: String)

case class Years(number: Int)

case class FullName(firstName: String, lastName: String)

case class WrappedWeather(weather: Weather)

object WrappedWeather {
  implicit val arbitrary: Arbitrary[WrappedWeather] =
    Arbitrary(Gen.resultOf(apply _))
}

object PersonId {
  implicit val mapper = TypeMapper(PersonId.apply)(_.untypedId)

  implicit val arbitrary: Arbitrary[PersonId] =
    Arbitrary(Gen.resultOf(apply _))
}

object Years {
  implicit val mapper = TypeMapper(Years.apply)(_.number)

  implicit val arbitrary: Arbitrary[Years] =
    Arbitrary(Gen.resultOf(apply _))
}

object FullName {
  implicit val mapper = TypeMapper[Name, FullName](n => FullName(n.getFirst, n.getLast))(fn =>
    Name(first = Some(fn.firstName), last = Some(fn.lastName)))
}

// We import this into the generated code using a file-level option.
object MisplacedMapper {
  implicit val weatherMapper = TypeMapper(WrappedWeather.apply)(_.weather)
}

trait DomainEvent {
  def personId: Option[PersonId]
  def optionalNumber: Option[Int]
  def repeatedNumber: Seq[Int]
  def requiredNumber: Int
}
