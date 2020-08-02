scalaVersion := "2.12.10"

val scalapbVersion = "0.10.8"

ThisBuild / crossScalaVersions := Seq(Scala212, Scala213)

skip in publish := true

sonatypeProfileName := "com.thesamet"

val Scala212 = "2.12.10"
val Scala213 = "2.13.2"

scalaVersion in ThisBuild := Scala213

lazy val codeGen = project
  .in(file("code-gen"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "scalapb-grpcweb-code-gen",
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "scalapb.grpcweb",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "compilerplugin" % scalapbVersion
    )
  )

def projDef(name: String, shebang: Boolean) =
  sbt
    .Project(name, new File(name))
    .enablePlugins(AssemblyPlugin)
    .dependsOn(codeGen)
    .settings(
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(
        prependShellScript = Some(
          sbtassembly.AssemblyPlugin.defaultUniversalScript(shebang = shebang)
        )
      ),
      skip in publish := true,
      Compile / mainClass := Some("scalapb.grpcweb.GrpcWebCodeGenerator")
    )

lazy val protocGenScalaGrpcWebUnix =
  projDef("protoc-gen-scalapb-grpcweb-unix", shebang = true)

lazy val protocGenScalaGrpcWebWindows =
  projDef("protoc-gen-scalapb-grpcweb-windows", shebang = false)

lazy val protocGenScalaGrpcWeb = project
  .settings(
    crossScalaVersions := List(Scala213),
    name := "protoc-gen-scalapb-grpcweb",
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false,
    crossPaths := false,
    addArtifact(
      Artifact("protoc-gen-scalapb-grpcweb", "jar", "sh", "unix"),
      assembly in (protocGenScalaGrpcWebUnix, Compile)
    ),
    addArtifact(
      Artifact("protoc-gen-scalapb-grpcweb", "jar", "bat", "windows"),
      assembly in (protocGenScalaGrpcWebWindows, Compile)
    ),
    autoScalaLibrary := false
  )

lazy val grpcweb = project
  .in(file("grpcweb"))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    crossScalaVersions := Seq(Scala212, Scala213),
    name := "scalapb-grpcweb",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapbVersion,
      "com.thesamet.scalapb" %%% "protobuf-runtime-scala" % "0.8.6"
    ),
    npmDependencies in Compile += "grpc-web" -> "1.0.7"
  )

inThisBuild(
  List(
    organization := "com.thesamet.scalapb.grpcweb",
    homepage := Some(url("https://github.com/scalapb/scalapb-grpcweb")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "thesamet",
        "Nadav Samet",
        "thesamet@gmail.com",
        url("http://www.thesamet.com")
      )
    )
  )
)
