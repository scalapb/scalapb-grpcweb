[![Snapshot Artifacts][Badge-SonatypeSnapshots]][Link-SonatypeSnapshots]

# scalapb-grpcweb

**Experimental grpc-web support for ScalaPB**

This library provides a code generator and runtime that enables calling gRPC services
from your Scala.js code using [grpc-web](https://github.com/grpc/grpc-web).

## Usage:

1. Add the following to your `project/plugins.sbt`:
    ```
    val grpcWebVersion = "0.6.2"
    
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    
    addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.3")
    
    libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.2"
    
    libraryDependencies += "com.thesamet.scalapb.grpcweb" %% "scalapb-grpcweb-code-gen" % grpcWebVersion
    
    addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.20.0")
    ```

2. Add a cross-project with the protos that will be shared by Scala.js and
   JVM:

   ```
   lazy val protos =
     crossProject(JSPlatform, JVMPlatform)
        .crossType(CrossType.Pure)
        .in(file("protos"))
        .settings(
          PB.protoSources in Compile := Seq(
            (baseDirectory in ThisBuild).value / "protos" / "src" / "main" / "protobuf"
          ),
          libraryDependencies += "com.thesamet.scalapb" %%% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion
        )
        .jvmSettings(
          libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
          PB.targets in Compile := Seq(
            scalapb.gen() -> (sourceManaged in Compile).value
          ),
        )
        .jsSettings(
          // publish locally and update the version for test
          libraryDependencies += "com.thesamet.scalapb.grpcweb" %%% "scalapb-grpcweb" % scalapb.grpcweb.BuildInfo.version,
          PB.targets in Compile := Seq(
            scalapb.gen(grpc=false) -> (sourceManaged in Compile).value,
            scalapb.grpcweb.GrpcWebCodeGenerator -> (sourceManaged in Compile).value
          )
        )
   ```

3. In your client code, instantiate the stub like this:

   ```
   val stub = TestServiceStub.stub(Channels.grpcwebChannel("http://localhost:8081"))
   ```

4. Now, you can call it like:

   ```
   // Make an async unary call
   stub.unary(req).onComplete {
     f => println("Unary" -> f)
   }

   // You can also pass metadata
   // Make sure header1 is accepted on the envoy config, otherwise the request will be rejected
   stub.unary(req, Metadata("header1" -> "value1")).onComplete {
     f => println("Unary" -> f)
   }

   // Make an async server streaming call
   stub.serverStreaming(req, new StreamObserver[Res] {
     override def onNext(value: Res): Unit = {
       println("Next: " + value)
     }

     override def onError(throwable: Throwable): Unit = {
       println("Error! " + throwable)
     }

     override def onCompleted(): Unit = {
       println("Completed!")
     }
   })
   ```

Check the [full example](https://github.com/scalapb/scalapb-grpcweb/tree/master/example)

[Link-SonatypeSnapshots]: https://oss.sonatype.org/content/repositories/snapshots/com/thesamet/scalapb/grpcweb/scalapb-grpcweb_sjs1_2.13/ "Sonatype Snapshots"
[Badge-SonatypeSnapshots]: https://img.shields.io/nexus/s/https/oss.sonatype.org/com.thesamet.scalapb.grpcweb/scalapb-grpcweb_sjs1_2.13.svg "Sonatype Snapshots"
