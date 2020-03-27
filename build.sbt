scalaVersion := "2.12.10"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    crossScalaVersions := Seq("2.11.12", "2.12.10", "2.13.1"),
    sonatypeProfileName := "com.thesamet",
    name := "scalapb-grpcweb",
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %%% "scalapb-runtime" % "0.10.3-SNAPSHOT"
    ),
    npmDependencies in Compile += "grpc-web" -> "1.0.5"
  )

inThisBuild(
  List(
    organization := "com.thesamet.scalapb",
    homepage := Some(url("https://github.com/scalapb/scalapb-grpcweb")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "thesamet",
        "Nadav Samet",
        "thesamet@gmail.com",
        url("http://www.thesamet.com")
      )
    )
  ))
