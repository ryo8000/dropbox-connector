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
import com.dropbox.core.v2.team.GroupMemberInfo;
import com.dropbox.core.v2.team.GroupSelector;
import com.dropbox.core.v2.team.GroupsListResult;
import com.dropbox.core.v2.team.GroupsMembersListResult;
import com.dropbox.core.v2.team.MembersListResult;
import com.dropbox.core.v2.team.TeamMemberInfo;
import com.dropbox.core.v2.teamcommon.GroupSummary;
import java.util.ArrayList;
import java.util.List;

/** The client class to make remote calls to the Dropbox API team endpoints. */
public final class TeamClient {

  /** Team client */
  private final DbxTeamClientV2 client;

  /** Get an instance of {@link TeamClient}. */
  public TeamClient(DbxRequestConfig requestConfig, DbxCredential credential) {
    this.client = new DbxTeamClientV2(requestConfig, credential);
  }

  /**
   * Fetch members of a team.
   *
   * @return team members.
   * @throws DbxException when fetching members from DropBox fails.
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

  /**
   * Fetch groups of a team.
   *
   * @return team groups.
   * @throws DbxException when fetching groups from DropBox fails.
   */
  public List<GroupSummary> getGroups() throws DbxException {
    GroupsListResult result = client.team().groupsList();
    List<GroupSummary> groups = new ArrayList<>();

    while (true) {
      groups.addAll(result.getGroups());
      if (!result.getHasMore()) {
        break;
      }
      result = client.team().groupsListContinue(result.getCursor());
    }
    return groups;
  }

  /**
   * Fetch group members of a team.
   *
   * @param groupId team group ID.
   * @return team group members.
   * @throws DbxException when fetching group members from DropBox fails.
   */
  public List<GroupMemberInfo> getGroupMembers(String groupId) throws DbxException {
    GroupSelector groupSelector = GroupSelector.groupId(groupId);
    GroupsMembersListResult result = client.team().groupsMembersList(groupSelector);
    List<GroupMemberInfo> groupMembers = new ArrayList<>();

    while (true) {
      groupMembers.addAll(result.getMembers());
      if (!result.getHasMore()) {
        break;
      }
      result = client.team().groupsMembersListContinue(result.getCursor());
    }
    return groupMembers;
  }
}
