package scalapb.grpc.grpcweb




import scala.annotation.meta.field
import scala.scalajs.js
import scala.scalajs.js.Dictionary
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.annotation.JSImport.Namespace
import scala.scalajs.js.typedarray.Uint8Array
import scalapb.grpc.grpcweb.Metadata.Metadata

@JSImport("grpc-web", Namespace)
@js.native
object grpcweb extends js.Object {
  @js.native
  class GrpcWebClientBase(opts: js.Any) extends js.Any {
    def rpcCall[Req, Res](
        method: String,
        request: Req,
        metadata: Metadata,
        methodInfo: AbstractClientBase.MethodInfo[Req, Res],
        callback: js.Function2[ErrorInfo, Res, Unit]
    ): ClientReadableStream = js.native

    def rpcCall[Req, Res](
        method: String,
        request: Req,
        metadata: Metadata,
        methodInfo: AbstractClientBase.MethodInfo[Req, Res]
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
    def on[T](
        `type`: String,
        callback: js.Function1[T, Unit]
    ): ClientReadableStream = js.native

    def on(`type`: String, callback: js.Function0[Unit]): ClientReadableStream =
      js.native

    def cancel(): Unit = js.native
  }

}

object Metadata {
  type Metadata = js.Dictionary[String]
  def apply(properties: Seq[(String, String)]): Metadata =  Dictionary.apply[String](properties: _*)
  def empty(): Metadata = Dictionary.empty[String]
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
