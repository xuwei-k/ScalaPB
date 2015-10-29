package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import scala.collection.JavaConverters._

final class ServicePrinter(service: ServiceDescriptor, override val params: GeneratorParams) extends DescriptorPimps {
  /**
   * [[https://github.com/grpc/grpc-java/blob/v0.9.0/compiler/src/java_plugin/cpp/java_generator.cpp#L564-L593]]
   */
  private[this] val imports = """"""

  /**
   * [[https://github.com/grpc/grpc-java/blob/v0.9.0/compiler/src/java_plugin/cpp/java_generator.cpp#L651]]
   */
  private[this] val javaServiceClassName = service.getName + "Grpc"
  private[this] val serviceClassName = service.getName + "GrpcScala"

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

  private[this] val javaServiceFull = {
    val init :+ last = service.getFile.scalaPackageName.split('.').toSeq
    (init :+ snakeCaseToCamelCase(last, true)).mkString(".")
  }

  private[this] val javaServiceGrpcFull = addPackageName(javaServiceClassName)

  private[this] val serviceJavaPackage = {
    val p = servicePackageName
    if(p.nonEmpty) {
      "package " + p
    } else {
      ""
    }
  }

  /**
   * [[https://github.com/grpc/grpc-java/blob/v0.9.0/compiler/src/java_plugin/cpp/java_generator.cpp#L73-L144]]
   */
  private[this] val methodFields = {

  }

  private[this] val serverClassName = "Scala" + service.getName + "Server"

  private[this] def clientClassName(suffix: String) = "Scala" + service.getName + suffix + "Client"
  private[this] val clientClassNameBase = clientClassName("")
  private[this] def clientClassImpl(suffix: String) = clientClassName(suffix) + "Impl"
  private[this] val blockingClientClass = clientClassImpl("Blocking")
  private[this] val asyncClientClass = clientClassImpl("Async")
  private[this] val javaAsyncClientClassName = javaServiceGrpcFull + "." + service.getName + "FutureClient"
  private[this] val javaBlockingClientClassName = javaServiceGrpcFull + "." + service.getName + "BlockingClient"

  private[this] val serverClass = {
    val methods = service.getMethods.asScala.map{ method =>
s"""    def ${snakeCaseToCamelCase(method.getName)}(request: ${method.getInputType.scalaTypeName}): scala.concurrent.Future[${method.getOutputType.scalaTypeName}]"""
    }.mkString("\n")

    val executionContext = "executionContext"

    val javaMethods = service.getMethods.asScala.map{ method =>
      val methodName = snakeCaseToCamelCase(method.getName)

s"""        override def ${methodName}(request: ${javaServiceFull}.${method.getInputType.getName}, observer: io.grpc.stub.StreamObserver[${javaServiceFull}.${method.getOutputType.getName}]): Unit = {
          self.${methodName}(${method.getInputType.scalaTypeName}.fromJavaProto(request)).onComplete {
            case scala.util.Success(value) =>
              observer.onNext(${method.getOutputType.scalaTypeName}.toJavaProto(value))
              observer.onCompleted()
            case scala.util.Failure(error) =>
              observer.onError(error)
              observer.onCompleted()
          }($executionContext)
        }"""
    }.mkString("\n")

    val build = s"""    final def build($executionContext: scala.concurrent.ExecutionContext): io.grpc.ServerServiceDefinition = {
      val s = new ${javaServiceGrpcFull}.${service.getName} {
$javaMethods
      }
      ${javaServiceGrpcFull}.bindService(s)
    }"""

s"""  trait $serverClassName { self =>
$methods

$build
  }"""
  }

  private[this] val clientClass = {
    def signature(outType: String => String) = { method: MethodDescriptor =>
      s"""    def ${snakeCaseToCamelCase(method.getName)}(request: ${method.getInputType.scalaTypeName}): ${outType(method.getOutputType.scalaTypeName)}"""
    }

    val F = "F[" + (_: String) + "]"
    val typeParam = F("_")

    val methods = service.getMethods.asScala.map(signature(F))

    val underlying = "underlying"

    val asyncMethodsImpl = service.getMethods.asScala.map{ method =>
      signature("scala.concurrent.Future[" + _ + "]")(method) + s""" = {
      guavaFuture2ScalaFuture($underlying.${snakeCaseToCamelCase(method.getName)}(${method.getInputType.scalaTypeName}.toJavaProto(request)))(${method.getOutputType.scalaTypeName}.fromJavaProto(_))
    }"""
    }

    val blockingMethodsImpl = service.getMethods.asScala.map{ method =>
      signature(identity)(method) + s""" = {
      ${method.getOutputType.scalaTypeName}.fromJavaProto($underlying.${snakeCaseToCamelCase(method.getName)}(${method.getInputType.scalaTypeName}.toJavaProto(request)))
    }"""
    }


s"""  trait $clientClassNameBase[$typeParam] {
${methods.mkString("\n")}
  }

  object $clientClassNameBase {
    def blocking(channel: io.grpc.Channel) = new $blockingClientClass(channel)
    def async(channel: io.grpc.Channel) = new $asyncClientClass(channel)
  }

  class $blockingClientClass(val $underlying: $javaBlockingClientClassName) extends $clientClassNameBase[({type l[a] = a})#l] {
    def this(channel: io.grpc.Channel) = {
      this($javaServiceGrpcFull.newBlockingStub(channel))
    }

${blockingMethodsImpl.mkString("\n")}
  }

  class $asyncClientClass(val $underlying: $javaAsyncClientClassName) extends $clientClassNameBase[scala.concurrent.Future] {
    def this(channel: io.grpc.Channel) = {
      this($javaServiceGrpcFull.newFutureStub(channel))
    }

${asyncMethodsImpl.mkString("\n")}
  }"""
  }

  private[this] val guavaFuture2ScalaFuture = {
s"""  private def guavaFuture2ScalaFuture[A, B](guavaFuture: com.google.common.util.concurrent.ListenableFuture[A])(converter: A => B): scala.concurrent.Future[B] = {
    val p = scala.concurrent.Promise[B]()
    com.google.common.util.concurrent.Futures.addCallback(guavaFuture, new com.google.common.util.concurrent.FutureCallback[A] {
      override def onFailure(t: Throwable) = p.failure(t)
      override def onSuccess(a: A) = p.success(converter(a))
    })
    p.future
  }"""
  }

  def printService: String = {
s"""$serviceJavaPackage

$imports

@javax.annotation.Generated(Array("by ScalaPB"))
object $serviceClassName {
  val SERVICE_NAME = "${service.getFullName}"

$guavaFuture2ScalaFuture

$serverClass

$clientClass
}
"""
  }

}
