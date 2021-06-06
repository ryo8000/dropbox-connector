# Google Cloud Search DropBox Connector

**Note:** This product is under construction. It is not yet finished.

The Google Cloud Search DropBox Connector is a DropBox Connector that uses the [Google Cloud Search SDK](https://developers.google.com/cloud-search). It allows you to search data stored in DropBox using [Google Cloud Search](https://workspace.google.com/products/cloud-search/).

- Using Google Cloud Search, you can search your data with high speed, high accuracy, and intuitively.
- Users only see search results for content they have access to.
- Google Cloud Search is available if you are using Google Workspace Business and Enterprise editions.
- This connector implements the [graph traversal strategy](https://developers.google.com/cloud-search/docs/guides/content-connector#graph-traversal) provided by the [Content Connector SDK](https://developers.google.com/cloud-search/docs/guides/content-connector).
- Before running the DropBox content connector, you must map the principals used in DropBox to identities in the Google Cloud Identity service.

## Build instructions

Building this connector requires the following development tools:

- Java SE Development Kit (JDK) version 1.8.0_20 or greater
- Apache Maven version 3.3.0 or greater.

1. Install the Google Cloud Search SDK

   For more details, please refer to [this GitHub page](https://github.com/google-cloudsearch/connector-sdk).

2. Build this DropBox Connector

   a. Build the ZIP file:

   ```
   mvn package
   ```

   (To skip the tests when building the connector, use `mvn package -DskipTests`)

   this command creates a ZIP file containing the connector and its dependencies with a name like `google-cloudsearch-dropbox-connector-0.0.1-SNAPSHOT.zip`.

3. Install the connector

   a. Copy the ZIP file to the location where you want to install the connector.

   b. Unzip the connector ZIP file. A directory with a name like `google-cloudsearch-dropbox-connector-0.0.1-SNAPSHOT` will be created.

   c. Change into this directory. You should see the connector jar file, `google-cloudsearch-dropbox-connector-0.0.1-SNAPSHOT.jar`, as well as a `lib` directory containing the connector's dependencies.

4. Configure the connector

   a. Create a file containing the connector configuration parameters.

   Example: "my.config"

   ```
   api.sourceId=<DATA_SOURCE_ID>
   api.identitySourceId=<IDENTITY_SOURCE_ID>
   api.customerId=<CUSTOMER_ID>
   api.serviceAccountPrivateKeyFile=<PATH_TO_SERVICE_ACCOUNT_KEY>
   dropbox.credentialFile=<PATH_TO_DROPBOX_CREDENTIAL_FILE>
   dropbox.teamMemberIds=<LIST_OF_TEAM_MEMBER_IDS>
   ```

   - `api.sourceId`: the ID of the data source to synchronize the data with.

   - `api.identitySourceId`: the ID of a Cloud Identity source.

   - `api.customerId`: the google customer ID.

   - `api.serviceAccountKey`: path to JSON file containing the credentials of a service account that can access the Google APIs.

   - `dropbox.credentialFile`: path to JSON file containing the credentials that can access the Dropbox APIs.

     Example: "my-app.json"

     ```
     {
        "access_token": <ACCESS_TOKEN>,
        "app_key": <APP_KEY>,
        "app_secret": <APP_SECRET>
     }
     ```

     - Preferably, access token expiration is "no expiration". Otherwise, the connector will stop processing during operation.

   - `dropbox.teamMemberIds`: List of team member IDs to be processed. The default is an empty string.

5. Run the connector

   The connector should be run from the unzipped installation directory, **not** the source code's `target` directory.

   a. Run the Identity connector

   Identity connectors are used to map your enterprise's identities and group rosters to the Google accounts and groups used by Google Cloud Search.

   ```
   java \
      -jar google-cloudsearch-dropbox-connector-0.0.1-SNAPSHOT.jar \
      --identity \
      -Dconfig=my.config
   ```

   Where `my.config` is the configuration file containing the parameters for the connector execution.

   **Note:** If the configuration file is not specified, a default file name of `connector-config.properties` will be assumed.

   b. Run the Content connector

   Content connectors are used to traverse a repository and index the data so that Google Cloud Search can effectively search that data.

   ```
   java \
      -jar google-cloudsearch-dropbox-connector-0.0.1-SNAPSHOT.jar \
      --dropbox \
      -Dconfig=my.config
   ```
