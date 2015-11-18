import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

name := "e2e"

PB.protobufSettings

PB.scalapbVersion in PB.protobufConfig := com.trueaccord.scalapb.Version.scalapbVersion

PB.javaConversions in PB.protobufConfig := true

(PB.runProtoc in PB.protobufConfig) := { args0 =>
  IO.withTemporaryDirectory{ dir =>
    val exe = dir / "grpc.exe"
    java.nio.file.Files.write(exe.toPath, grpcExe.value.get())
    exe.setExecutable(true)
    val args = args0 ++ Array(
      s"--plugin=protoc-gen-java_rpc=${exe.getAbsolutePath}",
      s"--java_rpc_out=${((sourceManaged in Compile).value / "compiled_protobuf").getAbsolutePath}"
    )
    println(s"runProtoc ${args.mkString(" ")}")
    com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)
  }
}

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "io.grpc" % "grpc-all" % "0.9.0",
  "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
  "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.Version.scalapbVersion % PB.protobufConfig
)

val grpcExe = SettingKey[xsbti.api.Lazy[Array[Byte]]]("grpcExeFile")

val grpcVersion = "0.9.0"

def grpcExeUrl() = {
  val os = if(scala.util.Properties.isMac){
    "osx-x86_64"
  }else if(scala.util.Properties.isWin){
    "windows-x86_64"
  }else{
    "linux-x86_64"
  }
  val artifactId = "protoc-gen-grpc-java"
  s"http://repo1.maven.org/maven2/io/grpc/${artifactId}/${grpcVersion}/${artifactId}-${grpcVersion}-${os}.exe"
}

grpcExe := xsbti.SafeLazy{
  IO.withTemporaryDirectory{ dir =>
    val f = dir / "temp.exe"
    val u = grpcExeUrl()
    println("download from " + u)
    IO.download(url(u), f)
    java.nio.file.Files.readAllBytes(f.toPath)
  }
}
