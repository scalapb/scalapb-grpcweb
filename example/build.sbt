import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

name := "scalapb-grpcweb-example"

scalaVersion in ThisBuild := "2.12.10"

val grpcWebVersion = "0.1.0+17-e71f75c7+20200327-1130-SNAPSHOT"

resolvers in ThisBuild ++= Seq(
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
)

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
        libraryDependencies += "com.thesamet.scalapb" %%% "scalapb-grpcweb" % grpcWebVersion
    )

lazy val protosJS = protos.js
lazy val protosJVM = protos.jvm

lazy val client = 
    project.in(file("client"))
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
        // This is an application with a main method
        scalaJSUseMainModuleInitializer := true,

      //  scalacOptions += "-P:scalajs:sjsDefinedByDefault",

       // libraryDependencies += "co.fs2" %%% "fs2-core" % "0.10.7"
    ).dependsOn(protosJS)

lazy val server = 
    project.in(file("server"))
    .settings(
        libraryDependencies ++= Seq(
            "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
        ),
    ).dependsOn(protosJVM)
