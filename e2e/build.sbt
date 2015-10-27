import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

name := "e2e"

PB.protobufSettings

PB.scalapbVersion in PB.protobufConfig := com.trueaccord.scalapb.Version.scalapbVersion

PB.javaConversions in PB.protobufConfig := true

PB.runProtoc in PB.protobufConfig := (args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "io.grpc" % "grpc-all" % "0.9.0",
  "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
  "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.Version.scalapbVersion % PB.protobufConfig
)

