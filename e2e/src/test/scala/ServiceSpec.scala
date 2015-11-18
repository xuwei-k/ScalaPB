import java.util.concurrent.{TimeUnit, TimeoutException}

import com.trueaccord.pb.Service1Impl
import com.trueaccord.proto.e2e.service._
import io.grpc.netty.{NegotiationType, NettyChannelBuilder, NettyServerBuilder}
import io.grpc.stub.StreamObserver
import io.grpc.{ManagedChannel, ServerServiceDefinition}
import org.scalatest.FlatSpec

import scala.concurrent.duration._
import scala.concurrent.{Future, Await, ExecutionContext, Promise}
import scala.util.Random

class ServiceSpec extends FlatSpec {

  private[this] val singleThreadExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    override def execute(runnable: Runnable): Unit = runnable.run()
  }

  private[this] def getObserverAndFuture[A]: (StreamObserver[A], Future[A]) = {
    val promise = Promise[A]()
    val observer = new StreamObserver[A] {
      override def onError(t: Throwable): Unit = {}
      override def onCompleted(): Unit = {}
      override def onNext(value: A): Unit = promise.success(value)
    }
    (observer, promise.future)
  }

  private[this] def randomString(): String = Random.alphanumeric.take(Random.nextInt(10)).mkString

  "method1 blockingClient" should "return request string length" in {
    withServer(Service1Grpc.bindService(new Service1Impl, singleThreadExecutionContext)){ channel =>
      val client = Service1Grpc.blockingClient(channel)
      val string = randomString()
      assert(client.method1(Req1(string)).length === string.length)
    }
  }

  "method1 futureClient" should "return request string length" in {
    withServer(Service1Grpc.bindService(new Service1Impl, singleThreadExecutionContext)){ channel =>
      val client = Service1Grpc.futureClient(channel)
      val string = randomString()
      assert(Await.result(client.method1(Req1(string)), 3.seconds).length === string.length)
    }
  }

  "method2" should "return message count" in {
    withServer(Service1Grpc.bindService(new Service1Impl, singleThreadExecutionContext)){ channel =>
      val client = Service1Grpc.futureClient(channel)
      val (responseObserver, future) = getObserverAndFuture[Res2]
      val requestObserver = client.method2(responseObserver)
      val n = Random.nextInt(10)
      for(_ <- 1 to n) {
        requestObserver.onNext(Req2())
      }

      try{
        Await.result(future, 3.seconds)
        fail("unexpected")
      }catch{
        case _: TimeoutException =>
      }

      requestObserver.onCompleted()
      assert(Await.result(future, 3.seconds).count === n)
    }
  }

  "method3" should "return response when reach limit" in {
    withServer(Service1Grpc.bindService(new Service1Impl, singleThreadExecutionContext)) { channel =>
      val client = Service1Grpc.futureClient(channel)
      val (observer, future) = getObserverAndFuture[Res3]
      val requests = Stream.continually(Req3(num = Random.nextInt(10)))
      val count = requests.scanLeft(0)(_ + _.num).takeWhile(_ < Service1Impl.method3Limit).size - 1

      requests.take(count).foreach{ req =>
        client.method3(req, observer)
      }

      try{
        Await.result(future, 3.seconds)
        fail("unexpected")
      }catch{
        case _: TimeoutException =>
      }

      client.method3(Req3(1000), observer)
      Await.result(future, 3.seconds)
    }
  }

  private[this] def withServer[A](services: ServerServiceDefinition*)(f: ManagedChannel => A): A = {
    val port = UniquePortGenerator.get()
    val server = services.foldLeft(NettyServerBuilder.forPort(port))(_.addService(_)).build()
    try{
      server.start()
      val channel = NettyChannelBuilder.forAddress("localhost", port).negotiationType(NegotiationType.PLAINTEXT).build()
      f(channel)
    } finally {
      server.shutdown()
      server.awaitTermination(3000, TimeUnit.MILLISECONDS)
    }
  }

}
