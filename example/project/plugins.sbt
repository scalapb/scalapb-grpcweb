addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "10.0.1")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.29")

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.17.0")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.2"
