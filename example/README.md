# scalapb-grpc-web example

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
   docker build -t envoy-scalapbexample . && docker run --network host -it envoy-scalapbexample
   ```

4. Open the `index.html` file from this directory in your browser.

   **NOTE**: All the output goes into the Javascript console. It is expected for the
   page to be blank!
