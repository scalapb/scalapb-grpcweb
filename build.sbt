val scalapbVersion = "0.11.17"

val Scala212 = "2.12.19"
val Scala213 = "2.13.14"
val Scala3 = "3.4.2"

ThisBuild / crossScalaVersions := Seq(Scala212, Scala213, Scala3)

publish / skip := true

sonatypeProfileName := "com.thesamet"

ThisBuild / scalaVersion := Scala212

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
      assembly / assemblyOption := (assembly / assemblyOption).value.copy(
        prependShellScript = Some(
          sbtassembly.AssemblyPlugin.defaultUniversalScript(shebang = shebang)
        )
      ),
      // prevents a conflict with the nowarn shipped by scala-collection-compat
      assembly / assemblyMergeStrategy := {
        case PathList(
              "scala",
              "annotation",
              "nowarn.class" | "nowarn$.class"
            ) =>
          MergeStrategy.first
        case x =>
          (assembly / assemblyMergeStrategy).value.apply(x)
      },
      publish / skip := true,
      Compile / mainClass := Some("scalapb.grpcweb.GrpcWebCodeGenerator")
    )

lazy val protocGenScalaGrpcWebUnix =
  projDef("protoc-gen-scalapb-grpcweb-unix", shebang = true)

lazy val protocGenScalaGrpcWebWindows =
  projDef("protoc-gen-scalapb-grpcweb-windows", shebang = false)

lazy val protocGenScalaGrpcWeb = project
  .settings(
    crossScalaVersions := List(Scala213, Scala3),
    name := "protoc-gen-scalapb-grpcweb",
    Compile / packageDoc / publishArtifact := false,
    Compile / packageSrc / publishArtifact := false,
    crossPaths := false,
    addArtifact(
      Artifact("protoc-gen-scalapb-grpcweb", "jar", "sh", "unix"),
      protocGenScalaGrpcWebUnix / Compile / assembly
    ),
    addArtifact(
      Artifact("protoc-gen-scalapb-grpcweb", "jar", "bat", "windows"),
      protocGenScalaGrpcWebWindows / Compile / assembly
    ),
    autoScalaLibrary := false
  )

lazy val grpcweb = project
  .in(file("grpcweb"))
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .settings(
    crossScalaVersions := Seq(Scala212, Scala213, Scala3),
    name := "scalapb-grpcweb",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapbVersion,
      "com.thesamet.scalapb" %%% "protobuf-runtime-scala" % "0.8.16"
    ),
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((3, _)) => List("-source:3.0-migration")
      case _            => Nil
    }),
    Compile / npmDependencies += "grpc-web" -> "1.4.2"
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
