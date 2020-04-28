scalaVersion := "2.12.10"

val scalapbVersion = "0.10.2"

lazy val codeGen = project
  .in(file("code-gen"))
  .settings(
    name := "scalapb-grpcweb-code-gen",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "compilerplugin" % scalapbVersion
    )
  )


lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    crossScalaVersions := Seq("2.12.10", "2.13.1"),
    sonatypeProfileName := "com.thesamet",
    name := "scalapb-grpcweb",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapbVersion,
      "com.thesamet.scalapb" %%% "protobuf-runtime-scala" % "0.8.5"
    ),
    npmDependencies in Compile += "grpc-web" -> "1.0.7"
  ).aggregate(codeGen)

inThisBuild(
  List(
    organization := "com.thesamet.scalapb",
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
