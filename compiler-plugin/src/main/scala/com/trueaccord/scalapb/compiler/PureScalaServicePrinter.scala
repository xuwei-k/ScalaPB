package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import scala.collection.JavaConverters._

final class PureScalaServicePrinter(service: ServiceDescriptor, override val params: GeneratorParams) extends DescriptorPimps {

  type Printer = FunctionalPrinter => FunctionalPrinter

  def Printer(f: Printer) = f

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

  private[this] val channel = "io.grpc.Channel"
  private[this] val callOptions = "io.grpc.CallOptions"

  private[this] def serviceName(p: String) = service.getName + "[" + p + "]"
  private[this] val serviceBlocking = serviceName("({type l[a] = a})#l")
  private[this] val serviceFuture = serviceName("scala.concurrent.Future")

  private[this] val futureUnaryCall = "io.grpc.stub.ClientCalls.futureUnaryCall"
  private[this] val blockingUnaryCall = "io.grpc.stub.ClientCalls.blockingUnaryCall"

  private[this] val clientImpl: Printer = { p =>
    val methods = service.getMethods.asScala.map{ m =>
      Printer{ p =>
        p.add(
          methodSig(m, identity) + " = {"
        ).add(
          s"""  $blockingUnaryCall(channel.newCall(null, options), request)""",
          "}"
        )
      }
    }

    val className = service.getName + "BlockingClientImpl"

    val build =
      s"  override def build(channel: $channel, options: $callOptions): $className = new $className(channel, options)"

    p.add(
      s"class $className(channel: $channel, options: $callOptions = $callOptions.DEFAULT) extends io.grpc.stub.AbstractStub[$className](channel, options) with $serviceBlocking {"
    ).withIndent(
      methods : _*
    ).add(
      build
    ).add(
      "}"
    )
  }

  def printService(printer: FunctionalPrinter): FunctionalPrinter = {
    printer.add(
      servicePackage,
      "",
      "import scala.language.higherKinds",
      "",
      s"object ${service.getName + "Grpc"} {"
    ).ln.withIndent(
      base,
      FunctionalPrinter.ln,
      clientImpl
    ).ln.addI(
      s"def blockingClient(channel: $channel): $serviceBlocking = ???",
      s"def futureClient(channel: $channel): $serviceFuture = ???"
    ).add(
      ""
    ).outdent.add("}")
  }
}
