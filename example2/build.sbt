import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

name := "scalapb-grpcweb-example"

scalaVersion in ThisBuild := "2.12.10"

val grpcWebVersion = "0.2.0"

resolvers in ThisBuild ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
)
// This is a generator for metadata support
lazy val addSettings : Seq[Setting[_]] = Seq(
    PB.targets in Compile := Seq(
        scalapb.gen() -> (sourceManaged in Compile).value,
          scalapb.grpc_web.GrpcWebWithMetadataCodeGenerator -> (sourceManaged in Compile).value))

lazy val protos = 
    crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("protos"))
    .settings(
        PB.protoSources in Compile := Seq(
            (baseDirectory in ThisBuild).value / "protos"/ "src" / "main" / "protobuf"),
        PB.targets in Compile := Seq(
            scalapb.gen() -> (sourceManaged in Compile).value
        ),

        libraryDependencies += "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
    )
    .jvmSettings(
        libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
    )
    .jsSettings(
        // publish locally and update the version for test
        libraryDependencies += "com.thesamet.scalapb" %%% "scalapb-grpcweb" % "0.2.0+13-6851bb06+20200422-1144-SNAPSHOT"
)

lazy val protosJS = protos.js.settings(addSettings : _*)
lazy val protosJVM = protos.jvm

lazy val client = 
    project.in(file("client"))
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
        // This is an application with a main method
        scalaJSUseMainModuleInitializer := true
    ).dependsOn(protosJS)

lazy val server = 
    project.in(file("server"))
    .settings(
        libraryDependencies ++= Seq(
            "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
        ),
    ).dependsOn(protosJVM)

