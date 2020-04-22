package scalapb.grpc_web

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import scalapb.compiler.FunctionalPrinter.PrinterEndo
import scalapb.compiler.ProtobufGenerator.asScalaDocBlock
import scalapb.compiler._
import scalapb.grpc_web.compat.JavaConverters._

final class GrpcServiceMetadataPrinter(
    service: ServiceDescriptor,
    implicits: DescriptorImplicits
) {

  import implicits._

  private[this] def observer(typeParam: String): String =
    s"$streamObserver[$typeParam]"

  private[this] def serviceMethodSignature(
      method: MethodDescriptor,
      overrideSig: Boolean
  ) = {
    val overrideStr = if (overrideSig) "override " else ""
    method.streamType match {
      case StreamType.Unary =>
        s"${method.deprecatedAnnotation}${overrideStr}def ${method.name}" + s"(request: ${method.inputType.scalaType}, metadata: $metadata): scala.concurrent.Future[${method.outputType.scalaType}]"
      case StreamType.ServerStreaming =>
        s"${method.deprecatedAnnotation}${overrideStr}def ${method.name}" + s"(request: ${method.inputType.scalaType}, metadata: $metadata, responseObserver: ${observer(
          method.outputType.scalaType)}): Unit"
      case _ =>
        ""
    }
  }

  private[this] def blockingMethodSignature(
      method: MethodDescriptor,
      overrideSig: Boolean
  ) = {
    val overrideStr = if (overrideSig) "override " else ""
    s"${method.deprecatedAnnotation}${overrideStr}def ${method.name}" + (method.streamType match {
      case StreamType.Unary =>
        s"(request: ${method.inputType.scalaType}, metadata: $metadata): ${method.outputType.scalaType}"
      case StreamType.ServerStreaming =>
        s"(request: ${method.inputType.scalaType}, metadata: $metadata): scala.collection.Iterator[${method.outputType.scalaType}]"
      case _ => throw new IllegalArgumentException("Invalid method type.")
    })
  }

  private[this] def serviceTrait: PrinterEndo = { p =>
    p.call(generateScalaDoc(service))
      .add(s"trait ${service.name} {")
      .indent
      .print(service.methods) {
        case (p, method) =>
          p.call(generateScalaDoc(method))
            .add(serviceMethodSignature(method, overrideSig = false))
      }
      .outdent
      .add("}")
  }

  private[this] val channel = "_root_.io.grpc.Channel"
  private[this] val callOptions = "_root_.io.grpc.CallOptions"

  private[this] val abstractStub = "_root_.io.grpc.stub.AbstractStub"
  private[this] val streamObserver = "_root_.io.grpc.stub.StreamObserver"

  private[this] val clientCalls = "_root_.scalapb.grpc.ClientCalls"

  private[this] val metadata = "_root_.scalapb.grpc.grpcweb.Metadata.Metadata"

  private[this] def methodDescriptor(method: MethodDescriptor) = PrinterEndo {
    p =>
      def marshaller(t: MethodDescriptorPimp#MethodTypeWrapper) =
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

  private[this] def clientMethodImpl(m: MethodDescriptor, blocking: Boolean) = {
    def printCall(p: FunctionalPrinter) = {
      val sig =
        if (blocking) blockingMethodSignature(m, overrideSig = true) + " = {"
        else serviceMethodSignature(m, overrideSig = true) + " = {"

      val prefix = if (blocking) "blocking" else "async"

      val methodName = prefix + (m.streamType match {
        case StreamType.Unary           => "UnaryCall"
        case StreamType.ServerStreaming => "ServerStreamingCall"
        case StreamType.ClientStreaming => "ClientStreamingCall"
        case StreamType.Bidirectional   => "BidiStreamingCall"
      })

      val args = Seq(
        "channel",
        m.grpcDescriptor.nameSymbol,
        "options",
        "metadata"
      ) ++
        (if (m.isClientStreaming) Seq() else Seq("request")) ++
        (if ((m.isClientStreaming || m.isServerStreaming) && !blocking)
           Seq("responseObserver")
         else Seq())

      val body = s"${clientCalls}.${methodName}(${args.mkString(", ")})"
      p.call(generateScalaDoc(m)).add(sig).addIndented(body).add("}").newline
    }

    PrinterEndo { p =>
      m.streamType match {
        case StreamType.Unary           => printCall(p)
        case StreamType.ServerStreaming => printCall(p)
        case _                          => p
      }
    }

  }

  private def stubImplementation(
      className: String,
      baseClass: String,
      methods: Seq[PrinterEndo]
  ): PrinterEndo = { p =>
    val build =
      s"override def build(channel: $channel, options: $callOptions): ${className} = new $className(channel, options)"
    p.add(
        s"class $className(channel: $channel, options: $callOptions = $callOptions.DEFAULT) extends $abstractStub[$className](channel, options) with $baseClass {"
      )
      .indent
      .call(methods: _*)
      .add(build)
      .outdent
      .add("}")
  }

  private[this] val stub: PrinterEndo = {
    val methods =
      service.getMethods.asScala.map(clientMethodImpl(_, false)).toSeq
    stubImplementation(service.stub, service.name, methods)
  }

  def printService(printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .add(
        "package " + service.getFile.scalaPackage.fullName,
        "",
        s"${service.deprecatedAnnotation}object ${service.companionObject.nameSymbol}{"
      )
      .indent
      .call(service.methods.map(methodDescriptor): _*)
      .call(serviceTrait)
      .newline
      .newline
      .call(stub)
      .newline
      .newline
      .add(
        s"def stub(channel: $channel): ${service.stub} = new ${service.stub}(channel)"
      )
      .newline
      .add(
        s"def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = ${service.javaDescriptorSource}"
      )
      .newline
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
