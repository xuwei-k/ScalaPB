package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import scala.collection.JavaConverters._

final class ServicePrinter(service: ServiceDescriptor) {
  /**
   * [[https://github.com/grpc/grpc-java/blob/v0.9.0/compiler/src/java_plugin/cpp/java_generator.cpp#L564-L593]]
   */
  val imports = """"""


  /**
   * [[https://github.com/grpc/grpc-java/blob/v0.9.0/compiler/src/java_plugin/cpp/java_generator.cpp#L651]]
   */
  val javaServiceClassName = service.getName + "Grpc"
  val serviceClassName = service.getName + "GrpcScala"

  /**
   * [[https://github.com/google/protobuf/blob/v3.0.0-beta-1/src/google/protobuf/compiler/java/java_helpers.cc#L224-L227]]
   * [[https://github.com/grpc/grpc-java/blob/v0.9.0/compiler/src/java_plugin/cpp/java_generator.cpp#L641-L648]]
   */
  val servicePackageName = service.getFullName.split('.').init.mkString(".")

  def addPackageName(s: String): String = {
    val p = servicePackageName
    if (p.nonEmpty) {
      p + "." + s
    } else {
      s
    }
  }

  val javaServiceFull = {
    val p = servicePackageName
    addPackageName(javaServiceClassName)
  }

  val serviceJavaPackage = {
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
  val methodFields = {

  }

  val serverClassName = "Scala" + service.getName + "Server"

  val clientClassName = "Scala" + service.getName + "Client"
  val clientClassImplName = clientClassName + "Impl"
  val javaClientClassName = javaServiceFull + "." + service.getName + "FutureClient"

  /**
   * [[https://github.com/grpc/grpc-java/blob/v0.9.0/compiler/src/java_plugin/cpp/java_generator.cpp#L523-L551]]
   */
  val stubMethods = {
s"""  def newScalaServer(channel: io.grpc.Channel): $serverClassName =
    new $serverClassName(channel)"""
  }

  val serverClass = {
    val methods = service.getMethods.asScala.map{ method =>
s"""    def ${method.getName}(request: ${method.getInputType.getFullName}): scala.concurrent.Future[${method.getOutputType.getFullName}]"""
    }.mkString("\n")

    val executionContext = "executionContext"

    val javaMethods = service.getMethods.asScala.map{ method =>
s"""        override def ${method.getName}(request: ${method.getInputType.getFullName}, observer: StreamObserver[${method.getOutputType.getFullName}]): Unit = {
          self.${method.getName}(request).onComplete {
            case scala.util.Success(value) =>
              observer.onNext(value)
              observer.onCompleted()
            case scala.util.Failure(error) =>
              observer.onError(error)
              observer.onCompleted()
          }($executionContext)
        }"""
    }.mkString("\n")

    val build = s"""    final def build($executionContext: scala.concurrent.ExecutionContext): io.grpc.ServerServiceDefinition = {
      val s = new ${javaServiceClassName} {
$javaMethods
      }
      ${javaServiceFull}.bindService(s)
    }"""

s"""  trait $serverClassName { self =>
$methods

$build
  }"""
  }

  val clientClass = {
    val signature = { method: MethodDescriptor =>
      s"""    def ${method.getName}(request: ${method.getInputType.getFullName}): scala.concurrent.Future[${method.getOutputType.getFullName}]"""
    }

    val methods = service.getMethods.asScala.map(signature)

    val underlying = "underlying"

    val methodsImpl = service.getMethods.asScala.map{ method =>
      signature(method) + s""" = {
      $underlying.${method.getName}(request)
    }"""
    }

s"""  trait $clientClassName {
${methods.mkString("\n")}
  }

  object $clientClassName {
    def get(channel: io.grpc.Channel) = new $clientClassImplName(channel)
  }

  class $clientClassImplName(val $underlying: $javaClientClassName) extends $clientClassName {
    def this(channel: io.grpc.Channel) = {
      this($javaServiceFull.newFutureStub(channel))
    }

${methodsImpl.mkString("\n")}
  }"""
  }


  def printService: String = {
s"""$serviceJavaPackage

$imports

@javax.annotation.Generated("by ScalaPB")
object $serviceClassName {
  val SERVICE_NAME = "${service.getFullName}"

$stubMethods

$serverClass

$clientClass
}
"""
  }

}
