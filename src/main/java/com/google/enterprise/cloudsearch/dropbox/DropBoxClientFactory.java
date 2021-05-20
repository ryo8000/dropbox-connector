/*
 * Copyright 2021 Ryo H
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.enterprise.cloudsearch.dropbox;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.oauth.DbxCredential;
import com.google.enterprise.cloudsearch.sdk.InvalidConfigurationException;

public final class DropBoxClientFactory {
  private static final String IDENTIFIER = "connector";

  private DropBoxClientFactory() {
  }

  public static DropBoxClient getTeamClient(String credentialFile) {
    DbxCredential credential = createCredential(credentialFile);
    DbxRequestConfig requestConfig = new DbxRequestConfig(IDENTIFIER);
    return new DropBoxClient(requestConfig, credential);
  }

  private static DbxCredential createCredential(String credentialFile) {
    DbxCredential credential;
    try {
      credential = DbxCredential.Reader.readFromFile(credentialFile);
    } catch (JsonReader.FileLoadException e) {
      throw new InvalidConfigurationException("Failed read credential file", e);
    }
    return credential;
  }
}
