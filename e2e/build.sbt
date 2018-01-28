import sbtcrossproject.CrossPlugin.autoImport.crossProject
import scalapb.compiler.Version.{protobufVersion, grpcJavaVersion}
scalaVersion in ThisBuild := "2.11.11"

val grpcArtifactId = "protoc-gen-grpc-java"

def grpcExeFileName = {
  val os = if (scala.util.Properties.isMac){
    "osx-x86_64"
  } else if (scala.util.Properties.isWin){
    "windows-x86_64"
  } else {
    "linux-x86_64"
  }
  s"${grpcArtifactId}-${grpcJavaVersion}-${os}.exe"
}

lazy val grpcExeUrl =
  url(s"http://repo1.maven.org/maven2/io/grpc/${grpcArtifactId}/${grpcJavaVersion}/${grpcExeFileName}")

val grpcExePath = SettingKey[xsbti.api.Lazy[File]]("grpcExePath")


val commonSettings = Seq(
    scalacOptions ++= Seq("-deprecation"),
    scalacOptions in Test ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)){
      case Some((2, v)) if v >= 11 =>
        Seq("-Ywarn-unused-import")
    }.toList.flatten,
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.3" % "test",
      "org.scalacheck" %%% "scalacheck" % "1.13.5" % "test",
      "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapb.Version.scalapbVersion % "compile,protobuf"
    ),
    PB.protocVersion := "-v351",
    PB.protoSources in Compile += file("shared/src/main/protobuf"),
    grpcExePath := xsbti.SafeLazy {
      val exe: File = (baseDirectory in ThisBuild).value / ".bin" / grpcExeFileName
      if (!exe.exists) {
        println("grpc protoc plugin (for Java) does not exist. Downloading.")
        IO.download(grpcExeUrl, exe)
        exe.setExecutable(true)
      } else {
        println("grpc protoc plugin (for Java) exists.")
      }
      exe
    })

lazy val root = crossProject(JSPlatform, JVMPlatform).in(file("."))
  .settings(commonSettings)
  .jvmSettings(
    javacOptions ++= Seq("-Xlint:deprecation"),
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-netty" % grpcJavaVersion, //netty transport of grpc
      "io.grpc" % "grpc-protobuf" % grpcJavaVersion, //protobuf message encoding for java implementation
      "io.grpc" % "grpc-services" % grpcJavaVersion,
      "io.grpc" % "grpc-services" % grpcJavaVersion % "protobuf",
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.7.0-rc1",
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.Version.scalapbVersion
    ),
    PB.protocOptions in Compile ++= Seq(
      s"--plugin=protoc-gen-java_rpc=${grpcExePath.value.get}",
      s"--java_rpc_out=${((sourceManaged in Compile).value).getAbsolutePath}"
    ),
    PB.targets in Compile := Seq(
      PB.gens.java(protobufVersion) -> (sourceManaged in Compile).value,
      scalapb.gen(javaConversions = true) -> (sourceManaged in Compile).value
    ),
    PB.protoSources in Compile += (PB.externalIncludePath in Compile).value / "io" / "grpc" / "reflection"
  )
  .jsSettings(
    PB.targets in Compile := Seq(
      scalapb.gen(javaConversions = false, grpc = false) -> (sourceManaged in Compile).value
    )
  )

lazy val jvm = root.jvm
lazy val js = root.js

lazy val noJava = (project in file("nojava"))
  .settings(commonSettings)
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.Version.scalapbVersion % "protobuf"
    )
  )
