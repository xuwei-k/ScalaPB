sonatypeSettings

fork in Test := true

val unusedWarnings = (
  "-Ywarn-unused" ::
  "-Ywarn-unused-import" ::
  Nil
)

scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-Xlint" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  "-Yno-adapted-args" ::
  Nil
)

scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
  case Some((2, v)) if v >= 11 => unusedWarnings
}.toList.flatten

Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) ~= {_.filterNot(unusedWarnings.toSet)}
)

baseDirectory in Test := (baseDirectory in LocalRootProject).value

resolvers += Resolver.url("typesafe-ivy-releases", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "3.0.0-beta-1",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "com.github.os72" % "protoc-jar" % "3.0.0-b1" % "test",
  "io.grpc" % "grpc-all" % "0.9.0" % "test",
  "org.scala-lang" % "scala-compiler" % scalaVersion.value % "test",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)

libraryDependencies += {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 10)) =>"org.scala-sbt" % "io" % "0.13.9" % "test"
    case Some((2, 11)) =>"org.scala-sbt" %% "io" % "0.13.9" % "test"
  }
}
