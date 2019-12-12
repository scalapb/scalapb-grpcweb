# scalapb-grpcweb

**Experimental grpc-web support for ScalaPB**

ScalaPB can generate gRPC clients and services from a protocol buffer
definition. 

This library provides a Scala.js runtime that makes the gRPC client generated
by ScalaPB work with [grpc-web](https://github.com/grpc/grpc-web).

## Usage:

1. Add `sbt-scalajs-bundler` to your `project/plugins.sbt`:

   ```
   addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.13.1")
   ```

   and enable it in your `build.sbt`:

   ```
   enablePlugins(ScalaJSBundlerPlugin)
   ```

2. Add a dependency on `scalapb-grpcweb` to your Scala.js project:

   ```
   libraryDependencies += "com.thesamet.scalapb" %%% "scalapb-grpcweb" % "0.1.0"
   ```

3. In your client code, instantiate the stub like this:

   ```
   val stub = new TestServiceStub(Channels.grpcwebChannel("http://localhost:8081"))
   ```

4. Now, you can call it like:

   ```
   // Make an async unary call
   stub.unary(req).onComplete {
     f => println("Unary", f)
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
