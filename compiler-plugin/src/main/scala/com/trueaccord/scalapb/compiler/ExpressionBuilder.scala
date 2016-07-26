package com.trueaccord.scalapb.compiler

sealed trait Expression extends Product with Serializable {
  def andThen(other: Expression) = (this, other) match {
    case (Identity, e2: LiteralExpression) => e2
    case (e1, Identity) => e1
    case (ExpressionList(l1), ExpressionList(l2)) => ExpressionList(l2 ++ l1)
    case (ExpressionList(l1), e: LiteralExpression) => ExpressionList(e :: l1)
    case (e: LiteralExpression, ExpressionList(l2)) => ExpressionList(l2 :+ e)
    case (e1: LiteralExpression, e2: LiteralExpression) => ExpressionList(e2 :: e1 :: Nil)
  }

  def apply(e: String, isCollection: Boolean, javaConversion: Boolean): String = ExpressionBuilder.run(this)(e, isCollection, javaConversion)
}

case class ExpressionList(l: List[LiteralExpression]) extends Expression

sealed trait LiteralExpression extends Expression {
  def isIdentity: Boolean
  def isFunctionApplication: Boolean
}

case object Identity extends LiteralExpression {
  def isIdentity: Boolean = true
  def isFunctionApplication: Boolean = false
}

case class FunctionApplication(name: String) extends LiteralExpression {
  def isIdentity: Boolean = false
  def isFunctionApplication: Boolean = true
}

case class MethodApplication(name: String) extends LiteralExpression {
  def isIdentity: Boolean = false
  def isFunctionApplication: Boolean = false
}

case class OperatorApplication(op: String) extends LiteralExpression {
  def isIdentity: Boolean = false
  def isFunctionApplication: Boolean = false
}

object ExpressionBuilder {
  def runSingleton(es: List[LiteralExpression])(e: String): String = es match {
    case Nil => e
    case Identity :: tail => runSingleton(tail)(e)
    case FunctionApplication(name) :: tail => s"$name(${runSingleton(tail)(e)})"
    case MethodApplication(name) :: tail => s"${runSingleton(tail)(e)}.$name"
    case OperatorApplication(name) :: tail => s"${runSingleton(tail)(e)} $name"
  }

  def runCollection(es: List[LiteralExpression], javaConversion: Boolean)(e: String): String = {
    val nontrivial = es.filterNot(_.isIdentity)
    val needVariable =
      nontrivial.filterNot(_.isIdentity)
        .dropRight(1).exists(_.isFunctionApplication)

    val asScala = if(javaConversion) ".asScala" else ""

    if (needVariable)
      s"""$e$asScala.map(__e => ${runSingleton(nontrivial)("__e")})"""
    else if (nontrivial.nonEmpty)
      s"""$e$asScala.map(${runSingleton(nontrivial)("_")})"""
    else e
  }

  def run(es: List[LiteralExpression])(e: String, isCollection: Boolean, javaConversion: Boolean): String =
    if (isCollection) runCollection(es, javaConversion)(e)
    else runSingleton(es)(e)

  def run(es: Expression)(e: String, isCollection: Boolean, javaConversion: Boolean): String = es match {
    case ExpressionList(l) => run(l)(e, isCollection, javaConversion)
    case expr: LiteralExpression => run(expr :: Nil)(e, isCollection, javaConversion)
  }
}
