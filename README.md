# Google Cloud Search DropBox Connector

The Google Cloud Search DropBox Connector enables indexing of content stored in a DropBox environment. This connector implements the [graph traversal strategy](https://developers.google.com/cloud-search/docs/guides/content-connector#graph-traversal) provided by the [Content Connector SDK](https://developers.google.com/cloud-search/docs/guides/content-connector).

Before running the DropBox content connector, you must map the principals used in DropBox to identities in the Google Cloud Identity service.

## Build instructions

1. Build the connector

   a. Build the ZIP file:

   ```
   mvn package
   ```

   (To skip the tests when building the connector, use `mvn package -DskipTests`)

2. Install the connector

   The `mvn package` command creates a ZIP file containing the connector and its dependencies with a name like `google-cloudsearch-dropbox-connector-0.0.1-SNAPSHOT.zip`.

   a. Copy this ZIP file to the location where you want to install the connector.

   b. Unzip the connector ZIP file. A directory with a name like `google-cloudsearch-dropbox-connector-0.0.1-SNAPSHOT` will be created.

   c. Change into this directory. You should see the connector jar file, `google-cloudsearch-dropbox-connector-0.0.1-SNAPSHOT.jar`, as well as a `lib` directory containing the connector's dependencies.

3. Configure the connector

   a. Create a file containing the connector configuration parameters.

4. Run the connector

   The connector should be run from the unzipped installation directory, **not** the source code's `target` directory.

   ```
   java \
      -jar google-cloudsearch-dropbox-connector-0.0.1-SNAPSHOT.jar \
      -Dconfig=my.config
   ```

   Where `my.config` is the configuration file containing the parameters for the connector execution.

   **Note:** If the configuration file is not specified, a default file name of `connector-config.properties` will be assumed.
