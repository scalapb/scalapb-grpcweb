package scalapb.grpc

import com.google.protobuf.Descriptors.FileDescriptor
import io.grpc.{CallOptions, Channel, MethodDescriptor, StatusRuntimeException, Status}
import io.grpc.protobuf.ProtoFileDescriptorSupplier
import io.grpc.stub.StreamObserver
import scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.Try

object Marshaller {
  def forMessage[T <: GeneratedMessage with Message[T]](implicit cmp: GeneratedMessageCompanion[T]): io.grpc.Marshaller[T] = new io.grpc.Marshaller[T] {
    override def toUint8Array(value: T): Uint8Array = {
      val ba = value.toByteArray
      val result = new Uint8Array(ba.length)
      var i = 0
      while (i < ba.length) {
        result(i) = ba(i)
        i += 1
      }
      result
    }

    override def fromUint8Array(value: Uint8Array): T = {
      val ba = new Array[Byte](value.length)
      var i = 0
      while (i < ba.length) {
        ba(i) = value(i).toByte
        i += 1
      }
      cmp.parseFrom(ba)
    }
  }
}

object Channels {
  def grpcwebChannel(url: String): Channel = new Channel {
    override val client = new grpcweb.grpcweb.GrpcWebClientBase()

    val baseUrl = url
  }
}

object Grpc {
  def completeObserver[T](observer: StreamObserver[T])(t: Try[T]): Unit = ???
}

trait AbstractService {
  def serviceCompanion: ServiceCompanion[_]
}

abstract class ServiceCompanion[T <: AbstractService] {
}

class ConcreteProtoFileDescriptorSupplier(f: => FileDescriptor) extends ProtoFileDescriptorSupplier

object ClientCalls {
  def asyncUnaryCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      request: ReqT
  ): Future[RespT] = {
    val p = Promise[RespT]
    val metadata: grpcweb.Metadata = new grpcweb.Metadata {}
    val handler: (grpcweb.ErrorInfo, RespT) => Unit = {
      (errorInfo: grpcweb.ErrorInfo, res: RespT) =>
        if (errorInfo != null)
          p.failure(new StatusRuntimeException(Status.fromErrorInfo(errorInfo)))
        else
          p.success(res)
    }
    channel.client.rpcCall[ReqT, RespT](
        channel.baseUrl + "/" + method.fullName, request, metadata, method.methodInfo, handler
    )
    p.future
  }

  def asyncServerStreamingCall[ReqT, RespT](
    channel: Channel,
    method: MethodDescriptor[ReqT, RespT],
    options: CallOptions,
    request: ReqT,
    responseObserver: StreamObserver[RespT]
  ): Unit = {
    val metadata: grpcweb.Metadata = new grpcweb.Metadata {}
    channel.client.rpcCall(channel.baseUrl + "/" + method.fullName, request, metadata, method.methodInfo)
      .on("data", {
        res: RespT =>
          responseObserver.onNext(res)
      })
      .on("status", {
        statusInfo: grpcweb.StatusInfo =>
          if (statusInfo.code != 0) {
            responseObserver.onError(new StatusRuntimeException(Status.fromStatusInfo(statusInfo)))
          } else {
            // Once https://github.com/grpc/grpc-web/issues/289 is fixed.
            responseObserver.onCompleted()
          }
      })
      .on("error", {
        errorInfo: grpcweb.ErrorInfo =>
          responseObserver.onError(new StatusRuntimeException(Status.fromErrorInfo(errorInfo)))
      })
      .on("end", {
        _: Any => 
          responseObserver.onCompleted()
      })
  }


  def blockingServerStreamingCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      request: ReqT
  ): Iterator[RespT] = ???

  def blockingUnaryCall[ReqT, RespT](
    channel: Channel,
    method: MethodDescriptor[ReqT, RespT],
    options: CallOptions,
    request: ReqT
  ): RespT = ???

  def asyncClientStreamingCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      responseObserver: StreamObserver[RespT]
  ): StreamObserver[ReqT] = ???

  def asyncBidiStreamingCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      responseObserver: StreamObserver[RespT]
  ): StreamObserver[ReqT] = ???
}
