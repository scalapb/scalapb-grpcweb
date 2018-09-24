package scalapb.grpc.grpcweb

import scala.annotation.meta.field
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport.Namespace
import scala.scalajs.js.annotation.{JSExport, JSImport, ScalaJSDefined}
import scala.scalajs.js.typedarray.Uint8Array


@JSImport("grpc-web", Namespace)
@js.native
object grpcweb extends js.Object {
  @js.native
  class GrpcWebClientBase(opts: js.Any) extends js.Any {
    def rpcCall[Req, Res](
      method: String, request: Req, metadata: Metadata, methodInfo: AbstractClientBase.MethodInfo[Req, Res], callback: js.Function2[ErrorInfo, Res, Unit]
    ): ClientReadableStream = js.native

    def rpcCall[Req, Res](
      method: String, request: Req, metadata: Metadata, methodInfo: AbstractClientBase.MethodInfo[Req, Res]
    ): ClientReadableStream = js.native
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
  class ClientReadableStream extends js.Any {
    def on[T](`type`: String, callback: js.Function1[T, Unit]): ClientReadableStream = js.native

    def on(`type`: String, callback: js.Function0[Unit]): ClientReadableStream = js.native

    def cancel(): Unit = js.native
  }
}

trait Metadata extends js.Object {
}

@js.native
trait ErrorInfo extends js.Object {
  var code: js.Any  // We sometimes get an Int, sometimes a String
  var message: String
}

@js.native
trait StatusInfo extends js.Object {
  var code: Int
  var details: String
}
