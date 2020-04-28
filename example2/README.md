# scalapb-grpc-web example

    This example for passing metadata headers, Only for client side js calls
    For testing you may need to publish locally the scalapb-grpcweb plugin till this branch is merged
       ```
       sbt publishLocal
       ```
    Publish should publish the scalapb-grpcweb and scalapb-grpcweb-code-gen repository locally
    update the build.sbt and plugins.sbt in the example2 with the published version.



1. Run the server

   ```
   sbt server/run
   ```

2. Build the client js:

   ```
   sbt client/fastOptJS::webpack
   ```

3. Start the envoy proxy server

   ```
   docker run --rm -ti --net=host -v $PWD/envoy.yaml:/etc/envoy/envoy.yaml envoyproxy/envoy:v1.13.1
   ```

4. Open the `index.html` file from this directory in your browser.

   **NOTE**: All the output goes into the Javascript console. It is expected for the
   page to be blank!

