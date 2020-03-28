package scalapb.grpc.example

import io.grpc.stub.StreamObserver
import scalapb.grpc.Channels
import scalapb.web.myservice.TestServiceGrpc
import scalapb.web.myservice.{Req, Res}
import scala.concurrent.ExecutionContext.Implicits.global

class GrpcError(code: String, message: String) extends RuntimeException(s"Grpc-web error ${code}: ${message}")

object Client {

  def main(args: Array[String]): Unit = {
    println("Hello world!")

    val stub = TestServiceGrpc.stub(Channels.grpcwebChannel("http://localhost:8080"))
    val req = Req(payload="Hello!", vals=Seq(-4000, -1, 17, 39, 4175))
    // val req = Req(payload="error", vals=Seq(-4000, -1, 17, 39, 4175))

    // Make an async unary call
    stub.unary(req).onComplete {
      f => println("Unary", f)
    }

    // Make an async server streaming call
    stub.serverStreaming(req, new StreamObserver[Res] {
      override def onNext(value: Res): Unit = {
        println("Next: " + value)
      }

      override def onError(throwable: Throwable): Unit = {
        println("Error! " + throwable)
      }

      override def onCompleted(): Unit = {
        println("Completed!")
      }
    })
  }
}
