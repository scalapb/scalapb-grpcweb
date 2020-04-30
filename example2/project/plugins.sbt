addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.1")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.29")

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.17.0")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.2"
// publish locally and update the version for test
libraryDependencies += "com.thesamet.scalapb" %% "scalapb-grpcweb-code-gen" % "0.2.0+18-d8ba44da+20200430-2220-SNAPSHOT"
