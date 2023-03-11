package io.grpc

import io.grpc.MethodDescriptor.MethodType
import io.grpc.protobuf.ProtoFileDescriptorSupplier
import scalapb.grpcweb.native.AbstractClientBase.MethodInfo
import scalapb.grpcweb.native.GrpcWebClientBase
import scalapb.grpcweb.native.{ClientReadableStream, ErrorInfo, StatusInfo}

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

  abstract class ClientCallStreamObserver[RespT](
      private val stream: ClientReadableStream[RespT]
  ) {
    def cancel(message: String, cause: Throwable): Unit =
      stream.cancel()
  }

  abstract class AbstractStub[S <: AbstractStub[S]](
      channel: Channel,
      options: CallOptions
  ) {
    def build(channel: Channel, options: CallOptions): S
  }

  object AbstractStub {
    trait StubFactory[T <: AbstractStub[T]] {
      def newStub(channel: Channel, options: CallOptions): T
    }
  }

  object ServerCalls {
    def asyncUnaryCall(stuff: Any*): Unit = ???

    def asyncServerStreamingCall(stuff: Any*): Unit = ???

    def asyncBidiStreamingCall(stuff: Any*): Unit = ???

    def asyncClientStreamingCall(stuff: Any*): Unit = ???

    trait UnaryRequestMethod[ReqT, RespT] {
      def invoke(request: ReqT, responseObserver: StreamObserver[RespT]): Unit
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

trait CallOptions {
  def withDeadline(deadline: Deadline): CallOptions = ???

  def withDeadlineAfter(
      duration: Long,
      unit: java.util.concurrent.TimeUnit
  ): CallOptions = ???
}

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

final class Status(code: Status.Code, description: String, cause: Throwable) {
  def withDescription(description: String) =
    new Status(code, description, cause)
  def withCause(cause: Throwable) = new Status(code, description, cause)

  def getCode(): Status.Code = code
  def getDescription(): String = description
  def getCause(): Throwable = cause
}

object Status {
  def fromErrorInfo(ei: ErrorInfo): Status = {
    val code =
      if (scalajs.js.typeOf(ei.code) == "string")
        ei.code.asInstanceOf[String].toInt
      else ei.code.asInstanceOf[Int]

    Status.fromCodeValue(code).withDescription(ei.message)
  }

  def fromStatusInfo(si: StatusInfo): Status =
    Status.fromCodeValue(si.code).withDescription(si.details)

  def formatThrowableMessage(status: Status): String =
    s"${status.getCode().name()}: ${status.getDescription()}"

  def fromCodeValue(codeValue: Int): Status = {
    StatusList
      .find(_.getCode().value() == codeValue)
      .getOrElse(UNKNOWN.withDescription("Unknown code " + codeValue))
  }

  final class Code private (_name: String, _value: Int) {
    def toStatus(): Status = {
      StatusList
        .find(_.getCode() == this)
        .getOrElse(
          throw new IllegalArgumentException(
            s"No status found for code ${name()}"
          )
        )
    }

    def name(): String = _name

    def value(): Int = _value
  }

  object Code {
    lazy val OK = new Code("OK", 0)
    lazy val CANCELLED = new Code("CANCELLED", 1)
    lazy val UNKNOWN = new Code("UNKNOWN", 2)
    lazy val INVALID_ARGUMENT = new Code("INVALID_ARGUMENT", 3)
    lazy val DEADLINE_EXCEEDED = new Code("DEADLINE_EXCEEDED", 4)
    lazy val NOT_FOUND = new Code("NOT_FOUND", 5)
    lazy val ALREADY_EXISTS = new Code("ALREADY_EXISTS", 6)
    lazy val PERMISSION_DENIED = new Code("PERMISSION_DENIED", 7)
    lazy val RESOURCE_EXHAUSTED = new Code("RESOURCE_EXHAUSTED", 8)
    lazy val FAILED_PRECONDITION = new Code("FAILED_PRECONDITION", 9)
    lazy val ABORTED = new Code("ABORTED", 10)
    lazy val OUT_OF_RANGE = new Code("OUT_OF_RANGE", 11)
    lazy val UNIMPLEMENTED = new Code("UNIMPLEMENTED", 12)
    lazy val INTERNAL = new Code("INTERNAL", 13)
    lazy val UNAVAILABLE = new Code("UNAVAILABLE", 14)
    lazy val DATA_LOSS = new Code("DATA_LOSS", 15)
    lazy val UNAUTHENTICATED = new Code("UNAUTHENTICATED", 16)

    lazy val values: Array[Code] = {
      Array(
        OK,
        CANCELLED,
        UNKNOWN,
        INVALID_ARGUMENT,
        DEADLINE_EXCEEDED,
        NOT_FOUND,
        ALREADY_EXISTS,
        PERMISSION_DENIED,
        RESOURCE_EXHAUSTED,
        FAILED_PRECONDITION,
        ABORTED,
        OUT_OF_RANGE,
        UNIMPLEMENTED,
        INVALID_ARGUMENT,
        UNAVAILABLE,
        DATA_LOSS,
        UNAUTHENTICATED
      )
    }

    def valueOf(value: String): Code = {
      values.find(_.name() == value) match {
        case Some(code) => code
        case _ =>
          throw new IllegalArgumentException(s"Unrecognized code: $value")
      }
    }
  }

  private lazy val StatusList =
    Code.values.map(code => new Status(code, null, null))

  lazy val OK = Code.OK.toStatus()
  lazy val CANCELLED = Code.CANCELLED.toStatus()
  lazy val UNKNOWN = Code.UNKNOWN.toStatus()
  lazy val INVALID_ARGUMENT = Code.INVALID_ARGUMENT.toStatus()
  lazy val DEADLINE_EXCEEDED = Code.DEADLINE_EXCEEDED.toStatus()
  lazy val NOT_FOUND = Code.NOT_FOUND.toStatus()
  lazy val ALREADY_EXISTS = Code.ALREADY_EXISTS.toStatus()
  lazy val PERMISSION_DENIED = Code.PERMISSION_DENIED.toStatus()
  lazy val RESOURCE_EXHAUSTED = Code.RESOURCE_EXHAUSTED.toStatus()
  lazy val FAILED_PRECONDITION = Code.FAILED_PRECONDITION.toStatus()
  lazy val ABORTED = Code.ABORTED.toStatus()
  lazy val OUT_OF_RANGE = Code.OUT_OF_RANGE.toStatus()
  lazy val UNIMPLEMENTED = Code.UNIMPLEMENTED.toStatus()
  lazy val INTERNAL = Code.INTERNAL.toStatus()
  lazy val UNAVAILABLE = Code.UNAVAILABLE.toStatus()
  lazy val DATA_LOSS = Code.DATA_LOSS.toStatus()
  lazy val UNAUTHENTICATED = Code.UNAUTHENTICATED.toStatus()
}

final class StatusRuntimeException(status: Status)
    extends RuntimeException(Status.formatThrowableMessage(status)) {
  def getStatus(): Status = status
}

final class StatusException(status: Status, trailers: Metadata)
    extends Exception(Status.formatThrowableMessage(status)) {
  def this(status: Status) = this(status, null)
  def getStatus(): Status = status
  def getTrailers(): Metadata = trailers
}

trait Attributes

trait ServerCall[Req, Res] {
  def getAuthority(): String = ???
  def getMethodDescriptor(): MethodDescriptor[Req, Res] = ???
  def getAttributes(): Attributes = ???
}

trait Deadline
