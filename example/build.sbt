import sbt.Keys.libraryDependencies
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

name := "scalapb-grpcweb-example"

ThisBuild / scalaVersion := "3.0.0-RC1"

ThisBuild / resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

lazy val protos =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("protos"))
    .settings(
      Compile / PB.protoSources := Seq(
        (ThisBuild / baseDirectory).value / "protos" / "src" / "main" / "protobuf"
      ),
      Compile / PB.targets := Seq(
        scalapb.gen() -> (Compile / sourceManaged).value
      ),
      libraryDependencies += "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
      scalacOptions += "-source:3.0-migration"
    )
    .jvmSettings(
      libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    )
    .jsSettings(
      // publish locally and update the version for test
      libraryDependencies += "com.thesamet.scalapb.grpcweb" %%% "scalapb-grpcweb" % scalapb.grpcweb.BuildInfo.version,
      Compile / PB.targets := Seq(
        scalapb.gen(grpc = false) -> (Compile / sourceManaged).value,
        scalapb.grpcweb.GrpcWebCodeGenerator -> (Compile / sourceManaged).value
      )
    )

lazy val protosJS = protos.js
lazy val protosJVM = protos.jvm

lazy val client =
  project
    .in(file("client"))
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(ScalaJSBundlerPlugin)
    .settings(
      // This is an application with a main method
      scalaJSUseMainModuleInitializer := true
    )
    .dependsOn(protosJS)

lazy val server =
  project
    .in(file("server"))
    .settings(
      libraryDependencies ++= Seq(
        "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion
      )
    )
    .dependsOn(protosJVM)
