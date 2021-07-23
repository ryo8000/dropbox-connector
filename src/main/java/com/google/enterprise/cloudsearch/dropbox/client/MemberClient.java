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

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.SharedFileMembers;
import com.dropbox.core.v2.sharing.SharedFolderMembers;
import com.google.common.collect.Lists;
import com.google.enterprise.cloudsearch.dropbox.model.SharingInfo;
import java.util.List;
import java.util.stream.Collectors;

/** The client class to make remote calls to the Dropbox API user endpoints. */
public final class MemberClient {

  /** Member client */
  private final DbxClientV2 client;

  /** Get an instance of {@link MemberClient} */
  MemberClient(DbxClientV2 client) {
    this.client = client;
  }

  /**
   * Fetch files and folders in the folder.
   *
   * @param path parent folder path
   * @return files and folders in the folder
   * @throws DbxException when fetching files and folders from DropBox fails
   */
  public List<Metadata> listFolder(String path) throws DbxException {
    ListFolderResult result = client.files().listFolder(path);
    List<Metadata> listFolder = Lists.newArrayList();

    while (true) {
      listFolder.addAll(result.getEntries());
      if (!result.getHasMore()) {
        break;
      }
      result = client.files().listFolderContinue(result.getCursor());
    }
    return listFolder;
  }

  /**
   * Fetch folder sharing information.
   *
   * @param sharedFolderId shared folder ID
   * @return folder sharing information
   * @throws DbxException when fetching folder sharing information from DropBox fails
   */
  public SharingInfo getFolderSharingInfo(String sharedFolderId) throws DbxException {
    SharedFolderMembers sharedFolderMembers = client.sharing().listFolderMembers(sharedFolderId);
    List<String> userIds = Lists.newArrayList();
    List<String> groupNames = Lists.newArrayList();

    while (true) {
      userIds.addAll(sharedFolderMembers.getUsers().stream()
          .map(userMember -> userMember.getUser().getTeamMemberId())
          .collect(Collectors.toList()));

      groupNames.addAll(sharedFolderMembers.getGroups().stream()
          .map(groupMember -> groupMember.getGroup().getGroupName())
          .collect(Collectors.toList()));

      if (sharedFolderMembers.getCursor() == null) {
        break;
      }
      sharedFolderMembers =
          client.sharing().listFolderMembersContinue(sharedFolderMembers.getCursor());
    }
    return new SharingInfo.Builder(userIds, groupNames).build();
  }

  /**
   * Fetch file sharing information.
   *
   * @param filePath file path
   * @return file sharing information
   * @throws DbxException when fetching file sharing information from DropBox fails
   */
  public SharingInfo getFileSharingInfo(String filePath) throws DbxException {
    SharedFileMembers sharedFileMembers = client.sharing().listFileMembers(filePath);
    List<String> userIds = Lists.newArrayList();
    List<String> groupNames = Lists.newArrayList();

    while (true) {
      userIds.addAll(sharedFileMembers.getUsers().stream()
          .map(userMember -> userMember.getUser().getTeamMemberId())
          .collect(Collectors.toList()));

      groupNames.addAll(sharedFileMembers.getGroups().stream()
          .map(groupMember -> groupMember.getGroup().getGroupName())
          .collect(Collectors.toList()));

      if (sharedFileMembers.getCursor() == null) {
        break;
      }
      sharedFileMembers =
          client.sharing().listFileMembersContinue(sharedFileMembers.getCursor());
    }
    return new SharingInfo.Builder(userIds, groupNames).build();
  }

  public DbxDownloader<FileMetadata> download(String path) throws DbxException {
    return client.files().download(path);
  }
}
