package scalapb.grpcweb

import scala.annotation.meta.field
import scala.scalajs.js
import scala.scalajs.js.Dictionary
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.annotation.JSImport.Namespace
import scala.scalajs.js.typedarray.Uint8Array

@JSImport("grpc-web", Namespace)
@js.native
object native extends js.Object {
  @js.native
  class GrpcWebClientBase(opts: js.Any) extends js.Any {
    def rpcCall[Req, Res](
        method: String,
        request: Req,
        metadata: Metadata,
        methodInfo: AbstractClientBase.MethodInfo[Req, Res],
        callback: js.Function2[ErrorInfo, Res, Unit]
    ): ClientReadableStream[Res] = js.native

    def rpcCall[Req, Res](
        method: String,
        request: Req,
        metadata: Metadata,
        methodInfo: AbstractClientBase.MethodInfo[Req, Res]
    ): ClientReadableStream[Res] = js.native

    def serverStreaming[Req, Res](
        method: String,
        request: Req,
        metadata: Metadata,
        methodInfo: AbstractClientBase.MethodInfo[Req, Res]
    ): ClientReadableStream[Res] = js.native
  }

  @js.native
  object AbstractClientBase extends js.Any {
    @js.native
    class MethodInfo[Req, Res](
        responseType: js.Any,
        requestSerializer: js.Function1[Req, Uint8Array],
        responseDeserializer: js.Function1[Uint8Array, Res]
    ) extends js.Any
  }

  @js.native
  class ClientReadableStream[Res] extends js.Any {
    def on[A](
        `type`: String,
        callback: js.Function1[A, Unit]
    ): ClientReadableStream[Res] = js.native

    def on(
        `type`: String,
        callback: js.Function0[Unit]
    ): ClientReadableStream[Res] =
      js.native

    def cancel(): Unit = js.native
  }

  @js.native
  trait ErrorInfo extends js.Object {
    var code: js.Any // We sometimes get an Int, sometimes a String
    var message: String
  }

  @js.native
  trait StatusInfo extends js.Object {
    var code: Int
    var details: String
  }
}
