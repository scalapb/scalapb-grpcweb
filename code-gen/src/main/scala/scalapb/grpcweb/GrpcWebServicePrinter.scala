package scalapb.grpcweb

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import scalapb.compiler.FunctionalPrinter.PrinterEndo
import scalapb.compiler.ProtobufGenerator.asScalaDocBlock
import scalapb.compiler._
import scala.jdk.CollectionConverters._

final class GrpcWebServicePrinter(
    service: ServiceDescriptor,
    implicits: DescriptorImplicits
) {

  import implicits._

  private val OuterObject =
    service.getFile.scalaPackage / (service.getName() + "GrpcWeb")

  private val BaseTrait = OuterObject / service.name

  def scalaFileName =
    OuterObject.fullName.replace('.', '/') + ".scala"

  private[this] def streamObserver(typeParam: String): String =
    s"_root_.io.grpc.stub.StreamObserver[$typeParam]"

  private[this] def clientCallStreamObserver(typeParam: String): String =
    s"_root_.io.grpc.stub.ClientCallStreamObserver[$typeParam]"

  def isSupported(m: MethodDescriptor): Boolean =
    (m.streamType == StreamType.Unary || m.streamType == StreamType.ServerStreaming)

  private[this] def serviceMethodSignature(
      method: MethodDescriptor,
      withContext: Boolean
  ): String = {
    val contextParam = if (withContext) s", context: $context" else ""
    method.streamType match {
      case StreamType.Unary =>
        s"${method.deprecatedAnnotation}def ${method.name}" + s"(request: ${method.inputType.scalaType}$contextParam): scala.concurrent.Future[${method.outputType.scalaType}]"
      case StreamType.ServerStreaming =>
        s"${method.deprecatedAnnotation}def ${method.name}" + s"(request: ${method.inputType.scalaType}$contextParam, responseObserver: ${streamObserver(
            method.outputType.scalaType
          )}): ${clientCallStreamObserver(method.outputType.scalaType)}"
      case _ =>
        throw new RuntimeException("Unexpected method type")
    }
  }

  private[this] def serviceTrait: PrinterEndo = { p =>
    p.call(generateScalaDoc(service))
      .add(s"trait ${BaseTrait.name}[-Context] {")
      .indent
      .print(service.methods.filter(isSupported)) { case (p, method) =>
        p.call(generateScalaDoc(method))
          .add(serviceMethodSignature(method, true))
          .add(serviceMethodSignature(method, false))
      }
      .outdent
      .add("}")
  }

  private[this] val channel = "_root_.io.grpc.Channel"
  private[this] val callOptions = "_root_.io.grpc.CallOptions"

  private[this] val abstractStub = "_root_.io.grpc.stub.AbstractStub"

  private[this] val clientCalls = "_root_.scalapb.grpc.ClientCalls"

  private[this] val metadata = "_root_.scalapb.grpcweb.Metadata"

  private[this] val context = "Context"

  private[this] def methodDescriptor(method: MethodDescriptor) =
    PrinterEndo { p =>
      def marshaller(t: ExtendedMethodDescriptor#MethodTypeWrapper) =
        if (t.customScalaType.isDefined)
          s"_root_.scalapb.grpc.Marshaller.forTypeMappedType[${t.baseScalaType}, ${t.scalaType}]"
        else
          s"_root_.scalapb.grpc.Marshaller.forMessage[${t.scalaType}]"

      val methodType = method.streamType match {
        case StreamType.Unary           => "UNARY"
        case StreamType.ClientStreaming => "CLIENT_STREAMING"
        case StreamType.ServerStreaming => "SERVER_STREAMING"
        case StreamType.Bidirectional   => "BIDI_STREAMING"
      }

      val grpcMethodDescriptor = "_root_.io.grpc.MethodDescriptor"

      p.add(
        s"""${method.deprecatedAnnotation}val ${method.grpcDescriptor.nameSymbol}: $grpcMethodDescriptor[${method.inputType.scalaType}, ${method.outputType.scalaType}] =
         |  $grpcMethodDescriptor.newBuilder()
         |    .setType($grpcMethodDescriptor.MethodType.$methodType)
         |    .setFullMethodName($grpcMethodDescriptor.generateFullMethodName("${service.getFullName}", "${method.getName}"))
         |    .setSampledToLocalTracing(true)
         |    .setRequestMarshaller(${marshaller(method.inputType)})
         |    .setResponseMarshaller(${marshaller(method.outputType)})
         |    .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(${method.javaDescriptorSource}))
         |    .build()
         |""".stripMargin
      )
    }

  private[this] def clientMethodImpl(m: MethodDescriptor): PrinterEndo = { p =>
    val (maybeObserver, methodName) = (m.streamType match {
      case StreamType.Unary => ("", "asyncUnaryCall")
      case StreamType.ServerStreaming =>
        (", responseObserver", "asyncServerStreamingCall")
      case _ => ???
    })

    val args = Seq(
      "channel",
      m.grpcDescriptor.nameSymbol,
      "options",
      "f(context)"
    ) ++
      (if (m.isClientStreaming) Seq() else Seq("request")) ++
      (if ((m.isClientStreaming || m.isServerStreaming))
         Seq("responseObserver")
       else Seq())

    val body = s"${clientCalls}.${methodName}(${args.mkString(", ")})"
    p.call(generateScalaDoc(m))
      .add(serviceMethodSignature(m, true) + " = {")
      .addIndented(body)
      .add("}")
      .add(serviceMethodSignature(m, false) + " =")
      .add(s"  ${m.name}(request, defaultContext$maybeObserver)")
      .newline
  }

  private def stubImplementation(
      className: String,
      baseClass: String,
      methods: Seq[PrinterEndo]
  ): PrinterEndo = { p =>
    p.add(
      s"private final class $className[$context](channel: $channel, f: $context => $metadata, defaultContext: => $context, options: $callOptions = $callOptions.DEFAULT) extends ${BaseTrait.nameSymbol}[$context] {"
    ).indent
      .call(methods: _*)
      .outdent
      .add("}")
  }

  private[this] val stub: PrinterEndo = {
    val methods =
      service.getMethods.asScala
        .filter(isSupported(_))
        .map(clientMethodImpl(_))
        .toSeq
    stubImplementation(service.stub, service.name, methods)
  }

  def printService(printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .add(
        "package " + service.getFile.scalaPackage.fullName,
        "",
        s"${service.deprecatedAnnotation}object ${OuterObject.nameSymbol} {"
      )
      .indent
      .call(service.methods.map(methodDescriptor): _*)
      .call(serviceTrait)
      .newline
      .call(stub)
      .newline
      .add(
        s"def stub(channel: $channel): ${BaseTrait.name}[$metadata] = new ${service.stub}[$metadata](channel, identity, $metadata.empty)"
      )
      .newline
      .add(
        s"def stub(channel: $channel, metadata: $metadata): ${BaseTrait.name}[$metadata] = new ${service.stub}[$metadata](channel, identity, $metadata.empty)"
      )
      .newline
      .add(
        s"def stub[Context](channel: $channel, f: Context => $metadata, defaultContext: Context): ${BaseTrait.name}[Context] = new ${service.stub}[Context](channel, f, defaultContext)"
      )
      .outdent
      .add("}")
  }

  def generateScalaDoc(service: ServiceDescriptor): PrinterEndo = { fp =>
    val lines = asScalaDocBlock(
      service.comment.map(_.split('\n').toSeq).getOrElse(Seq.empty)
    )
    fp.add(lines: _*)
  }

  def generateScalaDoc(method: MethodDescriptor): PrinterEndo = { fp =>
    val lines = asScalaDocBlock(
      method.comment.map(_.split('\n').toSeq).getOrElse(Seq.empty)
    )
    fp.add(lines: _*)
  }
}
