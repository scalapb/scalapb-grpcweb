package scalapb.grpc_web

import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import protocbridge.codegen.{CodeGenApp, CodeGenRequest, CodeGenResponse}
import scalapb.compiler.{DescriptorImplicits, FunctionalPrinter, NameUtils, ProtobufGenerator}
import scalapb.options.compiler.Scalapb
import scalapb.grpc_web.compat.JavaConverters._

object GrpcWebWithMetadataCodeGenerator extends CodeGenApp {
  override def registerExtensions(registry: ExtensionRegistry): Unit =
    Scalapb.registerAllExtensions(registry)

  def process(request: CodeGenRequest): CodeGenResponse = {
    ProtobufGenerator.parseParameters(request.parameter) match {
      case Right(params) =>
        val implicits =
          new DescriptorImplicits(params, request.allProtos)
        CodeGenResponse.succeed(
          request.filesToGenerate
            .collect {
              case file if !file.getServices.isEmpty =>
               new MetadataFilePrinter(implicits, file).result()
            }
        )
      case Left(error) =>
        CodeGenResponse.fail(error)
    }
  }
}

class MetadataFilePrinter(
    implicits: DescriptorImplicits,
    file: FileDescriptor
) {
  import implicits._

  private val OuterObject =
    file.scalaPackage / s"${NameUtils.snakeCaseToCamelCase(baseName(file.getName), true)}GrpcWithMetaData"

  def scalaFileName: String =
    OuterObject.fullName.replace('.', '/') + ".scala"

  def content: String = {
    val fp = new FunctionalPrinter()
       fp.print(file.getServices.asScala)((fp, s) =>
        new GrpcServiceMetadataPrinter(s,implicits).printService(fp)
      ).result()
  }

  def result(): CodeGeneratorResponse.File = {
    val b = CodeGeneratorResponse.File.newBuilder()
    b.setName(scalaFileName)
    b.setContent(content)
    b.build()
  }

}
