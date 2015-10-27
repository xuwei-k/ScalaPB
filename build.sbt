import ReleaseTransformations._

scalaVersion in ThisBuild := "2.10.6"

crossScalaVersions in ThisBuild := Seq("2.10.6", "2.11.7")

scalacOptions in ThisBuild ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 11 => List("-target:jvm-1.7")
    case _ => Nil
  }
}

javacOptions in ThisBuild ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 11 => List("-target", "7", "-source", "7")
    case _ => Nil
  }
}

organization in ThisBuild := "com.trueaccord.scalapb"

resolvers in ThisBuild +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  pushChanges,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true)
)

lazy val root =
  project.in(file("."))
    .settings(
      publishArtifact := false,
      publish := {},
      publishLocal := {}
    ).aggregate(
      runtimeJS, runtimeJVM, compilerPlugin, proptest, scalapbc)

lazy val runtime = crossProject.crossType(CrossType.Full).in(file("scalapb-runtime"))
  .settings(
    name := "scalapb-runtime",
    libraryDependencies ++= Seq(
      "com.trueaccord.lenses" %%% "lenses" % "0.4.4",
      "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
      "org.scalatest" %% "scalatest" % (if (scalaVersion.value.startsWith("2.12")) "2.2.5-M2" else "2.2.5") % "test"
    ),
    unmanagedResourceDirectories in Compile += baseDirectory.value / "../../protobuf"
  )
  .jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % "3.0.0-beta-1"
    )
  )
  .jsSettings(
    // Add JS-specific settings here
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %%% "protobuf-runtime-scala" % "0.1.3"
    )
  )

lazy val runtimeJVM = runtime.jvm
lazy val runtimeJS = runtime.js

lazy val compilerPlugin = project.in(file("compiler-plugin"))
  .dependsOn(runtimeJVM)

lazy val scalapbc = project.in(file("scalapbc"))
  .dependsOn(compilerPlugin, runtimeJVM)
  .settings(
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))
  )

lazy val proptest = project.in(file("proptest"))
  .dependsOn(runtimeJVM, compilerPlugin)
    .configs( ShortTest )
    .settings( inConfig(ShortTest)(Defaults.testTasks): _*)
    .settings(
      publishArtifact := false,
      publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
      testOptions += Tests.Argument(
      ),
      testOptions in ShortTest += Tests.Argument(
        // verbosity specified because of ScalaCheck #108.
        "-verbosity", "3",
        "-minSuccessfulTests", "10")
    )

lazy val ShortTest = config("short") extend(Test)

// For e2e test
val sbtPluginVersion = "0.5.8"

def genVersionFile(out: File, version: String): File = {
  out.mkdirs()
  val f = out / "Version.scala"
  val w = new java.io.FileOutputStream(f)
  w.write(s"""|// Generated by ScalaPB's build.sbt.
              |
              |package com.trueaccord.scalapb
              |
              |object Version {
              |  val sbtPluginVersion = "$sbtPluginVersion"
              |  val scalapbVersion = "$version"
              |}
              |""".stripMargin.getBytes("UTF-8"))
  w.close()
  f
}

val createVersionFile = TaskKey[Unit](
  "create-version-file", "Creates a file with the project version to be used by e2e.")

createVersionFile <<= (streams, baseDirectory, version in Compile) map {
  (streams, baseDirectory, version) =>
    val f1 = genVersionFile(baseDirectory / "e2e/project/project", version)
    streams.log.info(s"Created $f1")
    val f2 = genVersionFile(baseDirectory / "e2e/project/", version)
    streams.log.info(s"Created $f2")
}

