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

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxTeamClientV2;
import com.dropbox.core.v2.team.MembersListResult;
import com.dropbox.core.v2.team.TeamMemberInfo;

import java.util.ArrayList;
import java.util.List;

public final class TeamClient {

  private final DbxTeamClientV2 client;

  public TeamClient(DbxRequestConfig requestConfig, DbxCredential credential) {
    this.client = new DbxTeamClientV2(requestConfig, credential);
  }

  /**
   * Fetch members of a team.
   *
   * @return team members.
   * @throws DbxException when fetching members from DropBox fails
   */
  public List<TeamMemberInfo> getMembers() throws DbxException {
    MembersListResult result = client.team().membersList();
    List<TeamMemberInfo> members = new ArrayList<>();

    while (true) {
      members.addAll(result.getMembers());
      if (!result.getHasMore()) {
        break;
      }
      result = client.team().membersListContinue(result.getCursor());
    }
    return members;
  }
}
