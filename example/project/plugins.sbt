resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.7.1")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.3")

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.5")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.4"

// publish locally and update the version for test
libraryDependencies += "com.thesamet.scalapb.grpcweb" %% "scalapb-grpcweb-code-gen" % "0.6.2"
