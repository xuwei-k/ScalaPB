package com.trueaccord.pb

import com.trueaccord.proto.e2e.Service1Grpc.Service1
import com.trueaccord.proto.e2e.service.{Res1, Req1, Res2, Req2}

import scala.concurrent.Future

object Service1Impl extends Service1[Future]{
  override def hello(request: Req1): Future[Res1] =
    Future.successful(Res1.defaultInstance)

  override def foo(request: Req2): Future[Res2] =
    Future.successful(Res2.defaultInstance)
}
