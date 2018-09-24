organization in ThisBuild := "com.thesamet.scalapb"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSBundlerPlugin)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "scalapb-grpcweb",
    libraryDependencies ++= Seq(
        "com.thesamet.scalapb" %%% "scalapb-runtime" % "0.8.0"
    ),

    scalacOptions += "-P:scalajs:sjsDefinedByDefault",

    npmDependencies in Compile += "grpc-web" -> "0.4.0"
  )

inThisBuild(List(
  organization := "com.thesamet",
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
