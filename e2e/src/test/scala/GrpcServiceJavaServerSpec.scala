import java.util.concurrent.TimeoutException

import com.trueaccord.pb.Service1ScalaImpl
import com.trueaccord.proto.e2e.service.{Service1Grpc => Service1GrpcScala, _}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Random

class GrpcServiceJavaServerSpec extends GrpcServiceSpecBase {

  describe("java server") {

    it("method1 blockingClient") {
      withJavaServer { channel =>
        val client = Service1GrpcScala.blockingClient(channel)
        val string = randomString()
        assert(client.method1(Req1(string)).length === string.length)
      }
    }

    it("method1 futureClient") {
      withJavaServer { channel =>
        val client = Service1GrpcScala.futureClient(channel)
        val string = randomString()
        assert(Await.result(client.method1(Req1(string)), 3.seconds).length === string.length)
      }
    }

    it("method2") {
      withJavaServer { channel =>
        val client = Service1GrpcScala.futureClient(channel)
        val (responseObserver, future) = getObserverAndFuture[Res2]
        val requestObserver = client.method2(responseObserver)
        val n = Random.nextInt(10)
        for (_ <- 1 to n) {
          requestObserver.onNext(Req2())
        }

        intercept[TimeoutException]{
          Await.result(future, 3.seconds)
        }

        requestObserver.onCompleted()
        assert(Await.result(future, 3.seconds).count === n)
      }
    }

    it("method3") {
      withJavaServer { channel =>
        val client = Service1GrpcScala.futureClient(channel)
        val (observer, future) = getObserverAndFuture[Res3]
        val requests = Stream.continually(Req3(num = Random.nextInt(10)))
        val count = requests.scanLeft(0)(_ + _.num).takeWhile(_ < Service1ScalaImpl.method3Limit).size - 1

        requests.take(count).foreach { req =>
          client.method3(req, observer)
        }

        intercept[TimeoutException]{
          Await.result(future, 3.seconds)
        }

        client.method3(Req3(1000), observer)
        Await.result(future, 3.seconds)
      }
    }
  }

}
