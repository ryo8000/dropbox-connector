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
package com.google.enterprise.cloudsearch.dropbox.model;

import static com.google.common.base.Preconditions.checkState;

import com.google.enterprise.cloudsearch.sdk.InvalidConfigurationException;
import com.google.enterprise.cloudsearch.sdk.config.Configuration;
import java.util.Collections;
import java.util.List;

public class DropBoxConfiguration {

  private static final String CREDENTIAL_FILE = "dropbox.credentialFile";
  /** Configuration key for list of team member IDs to be processed */
  private static final String TEAM_MEMBER_IDS = "dropbox.teamMemberIds";

  private final String credentialFile;
  /** List of team member IDs to be processed */
  private final List<String> teamMemberIds;

  private DropBoxConfiguration() {
    String configCredentialFile = Configuration.getString(CREDENTIAL_FILE, "").get();
    if (configCredentialFile.isEmpty()) {
      throw new InvalidConfigurationException("credentialFile can not be empty");
    }
    this.credentialFile = configCredentialFile;
    this.teamMemberIds = Configuration
        .getMultiValue(TEAM_MEMBER_IDS, Collections.emptyList(), Configuration.STRING_PARSER).get();
  }

  public static DropBoxConfiguration fromConfiguration() {
    checkState(Configuration.isInitialized(), "configuration not initialized");
    return new DropBoxConfiguration();
  }

  public String getCredentialFile() {
    return credentialFile;
  }

  /** Gets list of team member IDs to be processed. */
  public List<String> getTeamMemberIds() {
    return teamMemberIds;
  }

  @Override
  public String toString() {
    return "DropBoxConfiguration [credentialFile="
        + credentialFile
        + ", teamMemberIds="
        + teamMemberIds
        + "]";
  }
}
