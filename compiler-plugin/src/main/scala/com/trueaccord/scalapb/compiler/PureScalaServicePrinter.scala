package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import scala.collection.JavaConverters._

final class PureScalaServicePrinter(service: ServiceDescriptor, override val params: GeneratorParams) extends DescriptorPimps {
  private[this] val serviceClassName = service.getName + "Grpc"

  type Printer = FunctionalPrinter => FunctionalPrinter

  /**
   * [[https://github.com/google/protobuf/blob/v3.0.0-beta-1/src/google/protobuf/compiler/java/java_helpers.cc#L224-L227]]
   * [[https://github.com/grpc/grpc-java/blob/v0.9.0/compiler/src/java_plugin/cpp/java_generator.cpp#L641-L648]]
   */
  private[this] val servicePackageName = service.getFullName.split('.').init.mkString(".")

  private[this] def addPackageName(s: String): String = {
    val p = servicePackageName
    if (p.nonEmpty) {
      p + "." + s
    } else {
      s
    }
  }

  private[this] val servicePackage = {
    val p = servicePackageName
    if(p.nonEmpty) {
      "package " + p
    } else {
      ""
    }
  }

  private[this] def methodName(method: MethodDescriptor): String = snakeCaseToCamelCase(method.getName)

  def methodSig(method: MethodDescriptor, t: String => String) = {
    s"def ${methodName(method)}(request: ${method.getInputType.scalaTypeName}): ${t(method.getOutputType.scalaTypeName)}"
  }

  private[this] def base: Printer = {
    val F = "F[" + (_: String) + "]"

    val methods: Printer = { p =>
      p.seq(service.getMethods.asScala.map(methodSig(_, F)))
    }

    { p =>
      p.add(s"trait ${service.getName}[${F("_")}] {").withIndent(methods).add("}")
    }
  }



  def printService(printer: FunctionalPrinter): FunctionalPrinter = {
    printer.add(
      servicePackage,
      "",
      s"object $serviceClassName {"
    ).withIndent(base).add(
      ""
    ).outdent.add("}")
  }
}
