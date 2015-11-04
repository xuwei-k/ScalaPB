package com.trueaccord.scalapb.compiler

import java.util.Locale

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import scala.collection.JavaConverters._

final class PureScalaServicePrinter(service: ServiceDescriptor, override val params: GeneratorParams) extends DescriptorPimps {

  type Printer = FunctionalPrinter => FunctionalPrinter

  private[this] def Printer(f: Printer) = f

  /**
   * [[https://github.com/google/protobuf/blob/v3.0.0-beta-1/src/google/protobuf/compiler/java/java_helpers.cc#L224-L227]]
   * [[https://github.com/grpc/grpc-java/blob/v0.9.0/compiler/src/java_plugin/cpp/java_generator.cpp#L641-L648]]
   */
  private[this] val servicePackage = {
    "package " + service.getFile.scalaPackageName
  }

  private[this] def methodName0(method: MethodDescriptor): String = snakeCaseToCamelCase(method.getName)
  private[this] def methodName(method: MethodDescriptor): String = methodName0(method).asSymbol
  private[this] def observer(typeParam: String): String = s"io.grpc.stub.StreamObserver[$typeParam]"

  private[this] def methodSig(method: MethodDescriptor, t: String => String = identity[String]): String = {
    method.streamType match {
      case StreamType.Unary =>
        s"def ${methodName(method)}(request: ${method.getInputType.scalaTypeName}): ${t(method.getOutputType.scalaTypeName)}"
      case StreamType.ServerStreaming =>
        s"def ${methodName(method)}(request: ${method.getInputType.scalaTypeName}, observer: ${observer(method.getOutputType.scalaTypeName)}): Unit"
      case StreamType.ClientStreaming | StreamType.Bidirectional =>
        s"def ${methodName(method)}(observer: ${observer(method.getOutputType.scalaTypeName)}): ${observer(method.getInputType.scalaTypeName)}"
    }
  }

  private[this] def base: Printer = {
    val F = "F[" + (_: String) + "]"

    val methods: Printer = { p =>
      p.seq(service.getMethods.asScala.map(methodSig(_, F)))
    }

    { p =>
      p.add(s"trait ${serviceName(F("_"))} {").withIndent(methods).add("}")
    }
  }

  private[this] val channel = "io.grpc.Channel"
  private[this] val callOptions = "io.grpc.CallOptions"

  private[this] def serviceName0 = service.getName.asSymbol
  private[this] def serviceName(p: String) = serviceName0 + "[" + p + "]"
  private[this] val serviceBlocking = serviceName("({type l[a] = a})#l")
  private[this] val serviceFuture = serviceName("scala.concurrent.Future")

  private[this] val futureUnaryCall = "io.grpc.stub.ClientCalls.futureUnaryCall"
  private[this] val blockingUnaryCall = "io.grpc.stub.ClientCalls.blockingUnaryCall"
  private[this] val abstractStub = "io.grpc.stub.AbstractStub"


  private[this] val asyncUnaryCall = "io.grpc.stub.ServerCalls.asyncUnaryCall"
  private[this] val asyncClientStreamingCall = "io.grpc.stub.ServerCalls.asyncClientStreamingCall"
  private[this] val asyncServerStreamingCall = "io.grpc.stub.ServerCalls.asyncServerStreamingCall"
  private[this] val asyncBidiStreamingCall = "io.grpc.stub.ServerCalls.asyncBidiStreamingCall"

  private implicit class MethodOps(val self: MethodDescriptor) {
    def streamType: StreamType = {
      val p = self.toProto
      (p.getClientStreaming, p.getServerStreaming) match {
        case (false, false) => StreamType.Unary
        case (true, false) => StreamType.ClientStreaming
        case (false, true) => StreamType.ServerStreaming
        case (true, true) => StreamType.Bidirectional
      }
    }
  }

  private[this] val blockingClientName: String = service.getName + "BlockingClientImpl"

  private[this] val blockingClientImpl: Printer = { p =>
    val methods = service.getMethods.asScala.map{ m =>
      m.streamType match {
        case StreamType.Unary =>
          Printer{ p =>
            p.add(
              "override " + methodSig(m, identity) + " = {"
            ).add(
              s"""  ${m.getOutputType.scalaTypeName}.fromJavaProto($blockingUnaryCall(channel.newCall(${methodDescriptorName(m)}, options), ${m.getInputType.scalaTypeName}.toJavaProto(request)))""",
              "}"
            )
          }
        case StreamType.ServerStreaming =>
          Printer{ p =>
            p.add(
              "override " + methodSig(m, identity) + " = { ??? }"
            )
          }
        case StreamType.ClientStreaming | StreamType.Bidirectional =>
          Printer{ p =>
            p.add(
              "override " + methodSig(m, identity) + " = { ??? }"
            )
          }
      }
    }

    val build =
      s"  override def build(channel: $channel, options: $callOptions): $blockingClientName = new $blockingClientName(channel, options)"

    p.add(
      s"class $blockingClientName(channel: $channel, options: $callOptions = $callOptions.DEFAULT) extends $abstractStub[$blockingClientName](channel, options) with $serviceBlocking {"
    ).withIndent(
      methods : _*
    ).add(
      build
    ).add(
      "}"
    )
  }

  private[this] val guavaFuture2ScalaFuture = "guavaFuture2ScalaFuture"

  private[this] val guavaFuture2ScalaFutureImpl = {
    s"""private[this] def $guavaFuture2ScalaFuture[A, B](guavaFuture: com.google.common.util.concurrent.ListenableFuture[A])(converter: A => B): scala.concurrent.Future[B] = {
    val p = scala.concurrent.Promise[B]()
    com.google.common.util.concurrent.Futures.addCallback(guavaFuture, new com.google.common.util.concurrent.FutureCallback[A] {
      override def onFailure(t: Throwable) = p.failure(t)
      override def onSuccess(a: A) = p.success(converter(a))
    })
    p.future
  }"""
  }

  private[this] val asyncClientName = service.getName + "AsyncClientImpl"

  private[this] val asyncClientImpl: Printer = { p =>
    val methods = service.getMethods.asScala.map{ m =>
      m.streamType match {
        case StreamType.Unary =>
          Printer { p =>
            p.add(
              "override " + methodSig(m, "scala.concurrent.Future[" + _ + "]") + " = {"
            ).add(
              s"""  $guavaFuture2ScalaFuture($futureUnaryCall(channel.newCall(${methodDescriptorName(m)}, options), ${m.getInputType.scalaTypeName}.toJavaProto(request)))(${m.getOutputType.scalaTypeName}.fromJavaProto(_))""",
              "}"
            )
          }
        case StreamType.ClientStreaming =>
          Printer { p =>
            p.add(
              "override " + methodSig(m) + " = { ???"
            ).add("}")
          }
        case StreamType.ServerStreaming =>
          Printer { p =>
            p.add(
              "override " + methodSig(m) + " = { ???"
            ).add("}")
          }
        case StreamType.Bidirectional =>
          Printer { p =>
            p.add(
              "override " + methodSig(m) + " = { ???"
            ).add("}")
          }
      }
    }

    val build =
      s"  override def build(channel: $channel, options: $callOptions): $asyncClientName = new $asyncClientName(channel, options)"

    p.add(
      s"class $asyncClientName(channel: $channel, options: $callOptions = $callOptions.DEFAULT) extends $abstractStub[$asyncClientName](channel, options) with $serviceFuture {"
    ).withIndent(
      methods : _*
    ).add(
      build
    ).add(
      "}"
    )
  }

  private[this] def methodDescriptorName(method: MethodDescriptor): String =
    "METHOD_" + method.getName.toUpperCase(Locale.ENGLISH)

  private[this] def methodDescriptor(method: MethodDescriptor) = {
    val inJava = method.getInputType.javaTypeName
    val outJava = method.getOutputType.javaTypeName

    def marshaller(typeName: String) =
      s"io.grpc.protobuf.ProtoUtils.marshaller($typeName.getDefaultInstance)"

    val methodType = method.streamType match {
      case StreamType.Unary => "UNARY"
      case StreamType.ClientStreaming => "CLIENT_STREAMING"
      case StreamType.ServerStreaming => "SERVER_STREAMING"
      case StreamType.Bidirectional => "BIDI_STREAMING"
    }

s"""  private[this] val ${methodDescriptorName(method)}: io.grpc.MethodDescriptor[$inJava, $outJava] =
    io.grpc.MethodDescriptor.create(
      io.grpc.MethodDescriptor.MethodType.$methodType,
      io.grpc.MethodDescriptor.generateFullMethodName("${service.getFullName}", "${method.getName}"),
      ${marshaller(inJava)},
      ${marshaller(outJava)}
    )"""
  }

  private[this] val methodDescriptors: Seq[String] = service.getMethods.asScala.map(methodDescriptor)

  private[this] def callMethodName(method: MethodDescriptor) =
    methodName0(method) + "Method"

  private[this] def callMethod(method: MethodDescriptor) =
    method.streamType match {
      case StreamType.Unary =>
        s"${callMethodName(method)}(service, executionContext)"
      case _ =>
        s"${callMethodName(method)}(service)"
    }

  private[this] def createMethod(method: MethodDescriptor): String = {
    val javaIn = method.getInputType.javaTypeName
    val javaOut = method.getOutputType.javaTypeName
    val executionContext = "executionContext"
    val name = callMethodName(method)
    val serviceImpl = "serviceImpl"
    method.streamType match {
      case StreamType.Unary =>
        val serverMethod = s"io.grpc.stub.ServerCalls.UnaryMethod[$javaIn, $javaOut]"
s"""  def ${name}($serviceImpl: $serviceFuture, $executionContext: scala.concurrent.ExecutionContext): $serverMethod = {
    new $serverMethod {
      override def invoke(request: $javaIn, observer: io.grpc.stub.StreamObserver[$javaOut]): Unit = {
        $serviceImpl.${methodName(method)}(${method.getInputType.scalaTypeName}.fromJavaProto(request)).onComplete {
          case scala.util.Success(value) =>
            observer.onNext(${method.getOutputType.scalaTypeName}.toJavaProto(value))
            observer.onCompleted()
          case scala.util.Failure(error) =>
            observer.onError(error)
            observer.onCompleted()
        }($executionContext)
      }
    }
  }"""
      case StreamType.ServerStreaming =>
        val serverMethod = s"io.grpc.stub.ServerCalls.ServerStreamingMethod[$javaIn, $javaOut]"

        s"""  def ${name}($serviceImpl: $serviceFuture): $serverMethod = {
    new $serverMethod {
      override def invoke(request: $javaIn, observer: io.grpc.stub.StreamObserver[$javaOut]): Unit = {
        ???
      }
    }
  }"""
      case StreamType.ClientStreaming =>
        val serverMethod = s"io.grpc.stub.ServerCalls.ClientStreamingMethod[$javaIn, $javaOut]"

        s"""  def ${name}($serviceImpl: $serviceFuture): $serverMethod = {
    new $serverMethod {
      override def invoke(observer: io.grpc.stub.StreamObserver[$javaOut]): io.grpc.stub.StreamObserver[$javaIn] = {
        ???
      }
    }
  }"""
      case StreamType.Bidirectional =>
        val serverMethod = s"io.grpc.stub.ServerCalls.BidiStreamingMethod[$javaIn, $javaOut]"

        s"""  def ${name}($serviceImpl: $serviceFuture): $serverMethod = {
    new $serverMethod {
      override def invoke(observer: io.grpc.stub.StreamObserver[$javaOut]): io.grpc.stub.StreamObserver[$javaIn] = {
        ???
      }
    }
  }"""
    }
  }

  private[this] val bindService = {
    val executionContext = "executionContext"
    val methods = service.getMethods.asScala.map { m =>

      val call = m.streamType match {
        case StreamType.Unary => asyncUnaryCall
        case StreamType.ClientStreaming => asyncClientStreamingCall
        case StreamType.ServerStreaming => asyncServerStreamingCall
        case StreamType.Bidirectional => asyncBidiStreamingCall
      }

s""".addMethod(
      ${methodDescriptorName(m)},
      $call(
        ${callMethod(m)}
      )
    )"""
    }.mkString

    val serverServiceDef = "io.grpc.ServerServiceDefinition"

s"""def bindService(service: $serviceFuture, $executionContext: scala.concurrent.ExecutionContext): $serverServiceDef =
    $serverServiceDef.builder("${service.getFullName}")$methods.build()
  """
  }

  val objectName = service.getName + "Grpc"

  def printService(printer: FunctionalPrinter): FunctionalPrinter = {
    printer.add(
      servicePackage,
      "",
      "import scala.language.higherKinds",
      "",
      s"object $objectName {"
    ).seq(
      service.getMethods.asScala.map(createMethod)
    ).seq(
      methodDescriptors
    ).ln.withIndent(
      base,
      FunctionalPrinter.ln,
      blockingClientImpl,
      FunctionalPrinter.ln,
      asyncClientImpl
    ).ln.addI(
      bindService,
      guavaFuture2ScalaFutureImpl,
      s"def blockingClient(channel: $channel): $serviceBlocking = new $blockingClientName(channel)",
      s"def futureClient(channel: $channel): $serviceFuture = new $asyncClientName(channel)"
    ).add(
      ""
    ).outdent.add("}")
  }
}
