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

