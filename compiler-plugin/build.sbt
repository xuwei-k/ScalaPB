sonatypeSettings

fork in Test := true

baseDirectory in Test := (baseDirectory in LocalRootProject).value

resolvers += Resolver.url("typesafe-ivy-releases", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)

libraryDependencies ++= Seq(
  "com.google.protobuf" % "protobuf-java" % "3.0.0-beta-1",
  "org.scala-sbt" %% "io" % "0.13.9" % "test",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "com.github.os72" % "protoc-jar" % "3.0.0-b1" % "test",
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
)
