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
package com.google.enterprise.cloudsearch.dropbox.client;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.oauth.DbxCredential;
import com.google.enterprise.cloudsearch.sdk.InvalidConfigurationException;

/** Factory class to return DropBox client. */
public class DropBoxClientFactory {
  private static final String IDENTIFIER = "connector";

  private DropBoxClientFactory() {
  }

  /**
   * Get an instance of {@link TeamClient}
   *
   * @param credentialFile dropBox credential file path
   * @return an instance of {@link TeamClient}
   */
  public static TeamClient getTeamClient(String credentialFile) {
    DbxCredential credential = createCredential(credentialFile);
    DbxRequestConfig requestConfig = new DbxRequestConfig(IDENTIFIER);
    return new TeamClient(requestConfig, credential);
  }

  /**
   * create an instance of {@link DbxCredential}
   *
   * @param credentialFile dropBox credential file path
   * @return an instance of {@link DbxCredential}
   */
  private static DbxCredential createCredential(String credentialFile) {
    DbxCredential credential;
    try {
      credential = DbxCredential.Reader.readFromFile(credentialFile);
    } catch (JsonReader.FileLoadException e) {
      throw new InvalidConfigurationException("Failed to read credential file", e);
    }
    return credential;
  }
}
