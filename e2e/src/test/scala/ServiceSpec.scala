import java.util.concurrent.TimeUnit

import com.trueaccord.pb.Service1Impl
import com.trueaccord.proto.e2e.Service1Grpc
import com.trueaccord.proto.e2e.service.Req1
import io.grpc.netty.{NegotiationType, NettyChannelBuilder, NettyServerBuilder}
import io.grpc.{ManagedChannel, ServerServiceDefinition}
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext

class ServiceSpec extends FlatSpec {

  private[this] val singleThreadExecutionContext = new ExecutionContext {
    override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
    override def execute(runnable: Runnable): Unit = runnable.run()
  }

  "Service" should "work" in {
    withServer(Service1Grpc.bindService(Service1Impl, singleThreadExecutionContext)){ channel =>
      val client = Service1Grpc.blockingClient(channel)
      client.hello(Req1())
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
