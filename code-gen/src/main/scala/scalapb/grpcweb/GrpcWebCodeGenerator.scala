package scalapb.grpcweb

import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import protocbridge.codegen.{CodeGenApp, CodeGenRequest, CodeGenResponse}
import scalapb.compiler._
import scalapb.options.compiler.Scalapb
import scala.jdk.CollectionConverters._

object GrpcWebCodeGenerator extends CodeGenApp {
  override def registerExtensions(registry: ExtensionRegistry): Unit =
    Scalapb.registerAllExtensions(registry)

  def process(request: CodeGenRequest): CodeGenResponse = {
    ProtobufGenerator.parseParameters(request.parameter) match {
      case Right(params) =>
        try {
          val implicits =
            new DescriptorImplicits(params, request.allProtos)
          val generatedFiles = request.filesToGenerate.flatMap { file =>
            generateServiceFiles(file, implicits)
          }
          CodeGenResponse.succeed(
            generatedFiles
          )
        } catch {
          case e: GeneratorException =>
            CodeGenResponse.fail(e.message)
        }
      case Left(error) =>
        CodeGenResponse.fail(error)
    }

  }

  private def generateServiceFiles(
      file: FileDescriptor,
      implicits: DescriptorImplicits
  ): Seq[CodeGeneratorResponse.File] = {
    import implicits._
    file.getServices.asScala.map { service =>
      val p = new GrpcWebServicePrinter(service, implicits)
      val code = p.printService(FunctionalPrinter()).result()
      CodeGeneratorResponse.File
        .newBuilder()
        .setName(p.scalaFileName)
        .setContent(code)
        .build
    }.toSeq
  }

}
