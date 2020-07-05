import sbt._
import Keys._
import Dependencies.versions
import sbtprotoc.ProtocPlugin.autoImport.PB
import sbtassembly.AssemblyPlugin.autoImport._

object BuildHelper {
  val compilerOptions = Seq(
    "-deprecation",
    "-target:jvm-1.8",
    "-feature",
    "-Xfatal-warnings",
    "-explaintypes",
    "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
    "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
    "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",        // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
    "-Ywarn-dead-code",              // Warn when dead code is identified.
    "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
    "-Ywarn-numeric-widen",          // Warn when numerics are widened.
    "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",          // Warn if a local definition is unused.
    "-Ywarn-unused:params",          // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",        // Warn if a private member is unused.
    "-Ywarn-value-discard",          // Warn when non-Unit expression results are unused.
    "-Ybackend-parallelism",
    "8",                                         // Enable paralellisation — change to desired number!
    "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
    "-Ycache-macro-class-loader:last-modified"   // and macro definitions. This can lead to performance improvements.
  )

  object Compiler {
    val generateVersionFile = Compile / sourceGenerators += Def.task {
      val file = (Compile / sourceManaged).value / "scalapb" / "compiler" / "Version.scala"
      IO.write(
        file,
        s"""package scalapb.compiler
           |object Version {
           |  val scalapbVersion = "${version.value}"
           |  val protobufVersion = "${versions.protobuf}"
           |  val grpcJavaVersion = "${versions.grpc}"
           |}""".stripMargin
      )
      Seq(file)
    }

    val generateEncodingFile = Compile / sourceGenerators += Def.task {
      val src =
        (LocalRootProject / baseDirectory).value / "scalapb-runtime" / "src" / "main" / "scala" / "scalapb" / "Encoding.scala"
      val dest =
        (Compile / sourceManaged).value / "scalapb" / "compiler" / "internal" / "Encoding.scala"
      val s = IO.read(src).replace("package scalapb", "package scalapb.internal")
      IO.write(dest, s"// DO NOT EDIT. Copy of $src\n\n" + s)
      Seq(dest)
    }

    val scalapbProtoPackageReplaceTask =
      TaskKey[Unit]("scalapb-proto-package-replace", "Replaces package name in scalapb.proto")

    val shadeProtoBeforeGenerate = Seq(
      Compile / scalapbProtoPackageReplaceTask := {
        streams.value.log
          .info(s"Generating scalapb.proto with package replaced to scalapb.options.compiler.")
        val src =
          (LocalRootProject / baseDirectory).value / "protobuf" / "scalapb" / "scalapb.proto"
        val dest = (Compile / resourceManaged).value / "protobuf" / "scalapb" / "scalapb.proto"
        val s    = IO.read(src).replace("scalapb.options", "scalapb.options.compiler")
        IO.write(dest, s"// DO NOT EDIT. Copy of $src\n\n" + s)
        Seq(dest)
      },
      Compile / PB.generate := ((Compile / PB.generate) dependsOn (Compile / scalapbProtoPackageReplaceTask)).value
    )

    val shadeTarget = settingKey[String]("Target to use when shading")

    val shadedLibSettings = Seq(
      shadeTarget in ThisBuild := s"scalapbshade.v${version.value.replaceAll("[.-]", "_")}.@0",
      assemblyShadeRules in assembly := Seq(
        ShadeRule.rename("scalapb.options.Scalapb**" -> shadeTarget.value).inProject,
        ShadeRule.rename("com.google.**"             -> shadeTarget.value).inAll
      ),
      assemblyExcludedJars in assembly := {
        val toInclude = Seq(
          "protobuf-java",
          "protoc-bridge"
        )
        (fullClasspath in assembly).value.filterNot { c =>
          toInclude.exists(prefix => c.data.getName.startsWith(prefix))
        }
      },
      artifact in (Compile, packageBin) := (artifact in (Compile, assembly)).value,
      pomPostProcess := { (node: scala.xml.Node) =>
        new scala.xml.transform.RuleTransformer(new scala.xml.transform.RewriteRule {
          override def transform(node: scala.xml.Node): scala.xml.NodeSeq =
            node match {
              case e: scala.xml.Elem
                  if e.label == "dependency" && e.child.exists(child =>
                    child.label == "artifactId" && child.text.startsWith("compilerplugin")
                  ) =>
                scala.xml.Comment(s"compilerplugin has been removed.")
              case _ => node
            }
        }).transform(node).head
      }
    ) ++ addArtifact(artifact in (Compile, packageBin), assembly),
  }

  val scalajsSourceMaps = scalacOptions += {
    val a = (baseDirectory in LocalRootProject).value.toURI.toString
    val g = "https://raw.githubusercontent.com/scalapb/ScalaPB/" + sys.process
      .Process("git rev-parse HEAD")
      .lineStream_!
      .head
    s"-P:scalajs:mapSourceURI:$a->$g/"
  }
}
