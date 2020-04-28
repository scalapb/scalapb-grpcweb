package scalapb.grpc_web

import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import protocbridge.codegen.{CodeGenApp, CodeGenRequest, CodeGenResponse}
import scalapb.compiler.{
  DescriptorImplicits,
  FunctionalPrinter,
  GeneratorException,
  GeneratorParams,
  NameUtils,
  ProtoValidation,
  ProtobufGenerator
}
import scalapb.grpc_web.compat.JavaConverters._
import scalapb.options.compiler.Scalapb

case class GrpcWebCodeGenerator(metadata: Boolean = false) extends CodeGenApp {
  override def registerExtensions(registry: ExtensionRegistry): Unit =
    Scalapb.registerAllExtensions(registry)

  def process(request: CodeGenRequest): CodeGenResponse = {
    ProtobufGenerator.parseParameters(request.parameter) match {
      case Right(params) =>
        try {
          val implicits =
            new DescriptorImplicits(params, request.allProtos)
          validate(request, implicits)
          val generatedFiles = request.filesToGenerate.flatMap { file =>
            if (metadata) {
              generateWithMetadata(params, file, implicits)
            } else {
              generate(params, file, implicits)
            }
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

  private def validate(
      request: CodeGenRequest,
      implicits: DescriptorImplicits
  ): Map[FileDescriptor, Scalapb.ScalaPbOptions] = {
    val validator = new ProtoValidation(implicits)
    validator.validateFiles(request.allProtos)
  }

  private def generate(
      params: GeneratorParams,
      file: FileDescriptor,
      implicits: DescriptorImplicits
  ): Seq[CodeGeneratorResponse.File] = {
    val generator = new ProtobufGenerator(params.copy(grpc = true), implicits)
    generator.generateMultipleScalaFilesForFileDescriptor(file)
  }

  private def generateWithMetadata(
      params: GeneratorParams,
      file: FileDescriptor,
      implicits: DescriptorImplicits
  ): Seq[CodeGeneratorResponse.File] = {
    val serviceFiles = generateServiceFiles(file, implicits)
    val generator = new ProtobufGenerator(params, implicits)
    val allOtherTypes =
      generator.generateMultipleScalaFilesForFileDescriptor(file)
    serviceFiles ++ allOtherTypes
  }

  private def generateServiceFiles(
      file: FileDescriptor,
      implicits: DescriptorImplicits
  ): Seq[CodeGeneratorResponse.File] = {
    import implicits._
    file.getServices.asScala.map { service =>
      val p = new GrpcServiceMetadataPrinter(service, implicits)
      val code = p.printService(FunctionalPrinter()).result()
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setName(
        file.scalaDirectory + "/" + service.companionObject.name + ".scala"
      )
      b.setContent(code)
      b.build
    }.toSeq
  }

}
