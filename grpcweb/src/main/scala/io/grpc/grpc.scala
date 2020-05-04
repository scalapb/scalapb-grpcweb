package io.grpc

import io.grpc.MethodDescriptor.MethodType
import io.grpc.protobuf.ProtoFileDescriptorSupplier
import scalapb.grpcweb.native.AbstractClientBase.MethodInfo
import scalapb.grpcweb.native.GrpcWebClientBase
import scalapb.grpcweb.native.{ErrorInfo, StatusInfo}

import scala.scalajs.js.typedarray.Uint8Array

package protobuf {
  trait ProtoFileDescriptorSupplier

  trait ProtoMethodDescriptorSupplier
}

package stub {
  trait StreamObserver[V] {
    def onNext(value: V): Unit

    def onError(throwable: Throwable): Unit

    def onCompleted(): Unit
  }

  abstract class AbstractStub[S <: AbstractStub[S]](
      channel: Channel,
      options: CallOptions
  ) {
    def build(channel: Channel, options: CallOptions): S
  }

  object ServerCalls {
    def asyncUnaryCall(stuff: Any*): Unit = ???

    def asyncServerStreamingCall(stuff: Any*): Unit = ???

    def asyncBidiStreamingCall(stuff: Any*): Unit = ???

    def asyncClientStreamingCall(stuff: Any*): Unit = ???

    trait UnaryRequestMethod[ReqT, RespT] {
      def invoke(request: ReqT, responseObserver: StreamObserver[RespT])
    }

    trait StreamingRequestMethod[ReqT, RespT] {
      def invoke(response: StreamObserver[RespT]): StreamObserver[ReqT]
    }

    trait UnaryMethod[ReqT, RespT] extends UnaryRequestMethod[ReqT, RespT]

    trait ServerStreamingMethod[ReqT, RespT]
        extends UnaryRequestMethod[ReqT, RespT]

    trait ClientStreamingMethod[ReqT, RespT]
        extends StreamingRequestMethod[ReqT, RespT]

    trait BidiStreamingMethod[ReqT, RespT]
        extends StreamingRequestMethod[ReqT, RespT]
  }
}

trait Channel {
  def client: GrpcWebClientBase

  def baseUrl: String
}

trait ManagedChannel extends Channel

trait CallOptions

object CallOptions {
  def DEFAULT: CallOptions = new CallOptions {}
}

trait Marshaller[T] {
  def toUint8Array(value: T): Uint8Array

  def fromUint8Array(value: Uint8Array): T
}

case class MethodDescriptor[Req, Res](
    methodType: MethodType,
    fullName: String,
    requestMarshaller: Marshaller[Req],
    responseMarshaller: Marshaller[Res]
) {
  val methodInfo: MethodInfo[Req, Res] = new MethodInfo[Req, Res](
    responseType = null,
    requestSerializer = requestMarshaller.toUint8Array(_: Req),
    responseDeserializer = responseMarshaller.fromUint8Array(_: Uint8Array)
  )
}

object MethodDescriptor {
  sealed trait MethodType

  object MethodType {
    case object UNARY extends MethodType
    case object SERVER_STREAMING extends MethodType
    case object CLIENT_STREAMING extends MethodType
    case object BIDI_STREAMING extends MethodType
  }

  def newBuilder[Req, Res](): Builder[Req, Res] = new Builder[Req, Res]

  def generateFullMethodName(serviceName: String, methodName: String) =
    s"${serviceName}/${methodName}"

  final class Builder[Req, Res] {
    var methodType: MethodType = null
    var requestMarshaller: Marshaller[Req] = null
    var responseMarshaller: Marshaller[Res] = null
    var fullName: String = null

    def setType(m: MethodType): Builder[Req, Res] = {
      methodType = m
      this
    }

    def setRequestMarshaller(m: Marshaller[Req]): Builder[Req, Res] = {
      requestMarshaller = m
      this
    }

    def setResponseMarshaller(m: Marshaller[Res]): Builder[Req, Res] = {
      responseMarshaller = m
      this
    }

    def setFullMethodName(fn: String): Builder[Req, Res] = {
      fullName = fn
      this
    }

    def setSampledToLocalTracing(b: Boolean) = {
      this
    }

    def setSchemaDescriptor(
        p: io.grpc.protobuf.ProtoMethodDescriptorSupplier
    ): Builder[Req, Res] = this

    def build(): MethodDescriptor[Req, Res] = {
      require(methodType != null)
      require(requestMarshaller != null)
      require(responseMarshaller != null)
      MethodDescriptor[Req, Res](
        methodType,
        fullName,
        requestMarshaller,
        responseMarshaller
      )
    }
  }
}

class ServiceDescriptor

object ServiceDescriptor {
  def newBuilder(serviceName: String): Builder = new Builder(serviceName)

  final class Builder(serviceName: String) {
    def setSchemaDescriptor(p: ProtoFileDescriptorSupplier): Builder = this
    def addMethod(method: MethodDescriptor[_, _]): Builder = this
    def build(): ServiceDescriptor = new ServiceDescriptor
  }
}

trait ServerServiceDefinition

object ServerServiceDefinition {
  def builder(service: ServiceDescriptor) = new Builder(service)

  class Builder(service: ServiceDescriptor) {
    def addMethod(stuff: Any*): Builder = this

    def build(): ServerServiceDefinition = new ServerServiceDefinition {}
  }
}

final case class Status(code: Int, description: String, cause: Throwable) {
  def withDescription(description: String) = copy(description = description)
  def withCause(cause: Throwable) = copy(cause = cause)
}

object Status {
  def fromErrorInfo(ei: ErrorInfo): Status = {
    val code =
      if (scalajs.js.typeOf(ei.code) == "string")
        ei.code.asInstanceOf[String].toInt
      else ei.code.asInstanceOf[Int]

    Status(code, ei.message, null)
  }

  def fromStatusInfo(si: StatusInfo): Status = Status(si.code, si.details, null)

  def formatThrowableMessage(status: Status): String =
    s"${status.code}: ${status.description}"

  val INTERNAL = Status(13, null, null)
}

final class StatusRuntimeException(status: Status)
    extends RuntimeException(Status.formatThrowableMessage(status))

final class Metadata
