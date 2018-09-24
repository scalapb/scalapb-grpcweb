organization in ThisBuild := "com.thesamet.scalapb"

scalaVersion := "2.12.6"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.6"),

    name := "scalapb-grpcweb",
    libraryDependencies ++= Seq(
        "com.thesamet.scalapb" %%% "scalapb-runtime" % "0.8.0"
    ),

    scalacOptions += "-P:scalajs:sjsDefinedByDefault",

    npmDependencies in Compile += "grpc-web" -> "0.4.0"
  )

inThisBuild(List(
  organization := "com.thesamet.scalapb",
  homepage := Some(url("https://github.com/scalapb/scalapb-grpcweb")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "thesamet",
      "Nadav Samet",
      "thesamet@gmail.com",
      url("http://www.thesamet.com")
    )
  )
))
