package com.trueaccord.scalapb

object OptionUtil {
  def getOrElse[A](option: Option[A], default: A): A = {
    option match {
      case Some(value) =>
        value
      case None =>
        default
    }
  }
}
