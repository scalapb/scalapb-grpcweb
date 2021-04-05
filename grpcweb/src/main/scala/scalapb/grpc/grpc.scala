package scalapb.grpc

import com.google.protobuf.Descriptors
import io.grpc.protobuf.{
  ProtoFileDescriptorSupplier,
  ProtoMethodDescriptorSupplier
}
import io.grpc.stub.{ClientCallStreamObserver, StreamObserver}
import io.grpc._
import scalapb.grpcweb.Metadata
import scalapb.grpcweb.native.{ClientReadableStream, ErrorInfo, StatusInfo}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js.Dictionary
import scala.scalajs.js.typedarray.Uint8Array
import scala.util.Try

object Marshaller {
  def forMessage[T <: GeneratedMessage](implicit
      cmp: GeneratedMessageCompanion[T]
  ): io.grpc.Marshaller[T] =
    new io.grpc.Marshaller[T] {
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
  def grpcwebChannel(url: String, binary: Boolean = false): ManagedChannel = {
    val opts = if (binary) Dictionary("format" -> "binary") else Dictionary()
    new ManagedChannel {
      override val client = new scalapb.grpcweb.native.GrpcWebClientBase(opts)

      val baseUrl = url
    }
  }
}

object Grpc {
  def completeObserver[T](observer: StreamObserver[T])(t: Try[T]): Unit = ???
}

trait AbstractService {
  def serviceCompanion: ServiceCompanion[_]
}

abstract class ServiceCompanion[T <: AbstractService] {}

class ConcreteProtoFileDescriptorSupplier(f: => Descriptors.FileDescriptor)
    extends ProtoFileDescriptorSupplier {}

class ConcreteProtoMethodDescriptorSupplier()
    extends ProtoMethodDescriptorSupplier {}

object ConcreteProtoMethodDescriptorSupplier {
  def fromMethodDescriptor(
      methodDescriptor: Descriptors.MethodDescriptor
  ): ConcreteProtoMethodDescriptorSupplier =
    new ConcreteProtoMethodDescriptorSupplier()
}

class AsyncClientCallStreamObserver[RespT](
    private val stream: ClientReadableStream[RespT]
) extends ClientCallStreamObserver(stream)

object ClientCalls {
  def asyncUnaryCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      metadata: Metadata,
      request: ReqT
  ): Future[RespT] = {
    val p = Promise[RespT]()
    val handler: (ErrorInfo, RespT) => Unit = {
      (errorInfo: ErrorInfo, res: RespT) =>
        if (errorInfo != null)
          p.failure(new StatusRuntimeException(Status.fromErrorInfo(errorInfo)))
        else
          p.success(res)
    }
    channel.client.rpcCall[ReqT, RespT](
      channel.baseUrl + "/" + method.fullName,
      request,
      metadata,
      method.methodInfo,
      handler
    )
    p.future
  }

  def asyncServerStreamingCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      metadata: Metadata,
      request: ReqT,
      responseObserver: StreamObserver[RespT]
  ): ClientCallStreamObserver[RespT] = {
    val stream = channel.client
      .serverStreaming(
        channel.baseUrl + "/" + method.fullName,
        request,
        metadata,
        method.methodInfo
      )
      .on("data", { res: RespT => responseObserver.onNext(res) })
      .on(
        "status",
        { statusInfo: StatusInfo =>
          if (statusInfo.code != 0) {
            responseObserver.onError(
              new StatusRuntimeException(Status.fromStatusInfo(statusInfo))
            )
          } else {
            // Once https://github.com/grpc/grpc-web/issues/289 is fixed.
            responseObserver.onCompleted()
          }
        }
      )
      .on(
        "error",
        { errorInfo: ErrorInfo =>
          responseObserver
            .onError(
              new StatusRuntimeException(Status.fromErrorInfo(errorInfo))
            )
        }
      )
      .on("end", { _: Any => responseObserver.onCompleted() })

    new AsyncClientCallStreamObserver(stream)
  }

  def asyncUnaryCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      request: ReqT
  ): Future[RespT] = ???

  def asyncServerStreamingCall[ReqT, RespT](
      channel: Channel,
      method: MethodDescriptor[ReqT, RespT],
      options: CallOptions,
      request: ReqT,
      responseObserver: StreamObserver[RespT]
  ): ClientCallStreamObserver[RespT] = ???

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
}
