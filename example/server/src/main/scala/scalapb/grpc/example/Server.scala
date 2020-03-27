package scalapb.grpc.example

import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import scalapb.web.myservice.{Req, Res, TestServiceGrpc}
import scalapb.web.myservice.TestServiceGrpc.TestService

import scala.concurrent.{ExecutionContext, Future}

object Server {
  def main(args: Array[String]): Unit = {
    val server = ServerBuilder
      .forPort(50051)
      .addService(
        TestServiceGrpc.bindService(new MyServiceImpl, ExecutionContext.global)
      )
      .build()
      .start()
    sys.addShutdownHook {
      server.shutdown()
    }
    server.awaitTermination()
  }
}

class MyServiceImpl extends TestService {
  override def unary(request: Req): Future[Res] = {
    println(request.vals)
    Future.successful(Res(request.payload.length, vals = request.vals))
  }

//  override def serverStreaming(request: Req,
//    responseObserver: StreamObserver[Res]): Unit = {
//    responseObserver.onNext(Res(payload = request.payload.length))
//    responseObserver.onNext(Res(payload = request.payload.length + 1))
//    responseObserver.onNext(Res(payload = request.payload.length + 2))
//    if (request.payload == "error") {
//      responseObserver.onError(new RuntimeException("Problem Problem"))
//    } else {
//      responseObserver.onNext(Res(payload = request.payload.length + 10))
//      responseObserver.onCompleted()
//    }
//  }
//
//  override def bidiStreaming(
//    responseObserver: StreamObserver[Res]): StreamObserver[Req] = ???
//
//  override def clientStreaming(
//    responseObserver: StreamObserver[Res]): StreamObserver[Req] = ???

}
