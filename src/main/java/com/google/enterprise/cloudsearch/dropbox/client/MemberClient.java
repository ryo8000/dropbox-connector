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
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import java.util.ArrayList;
import java.util.List;

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
   * @return files and folders in the folder.
   * @throws DbxException when fetching files and folders from DropBox fails.
   */
  public List<Metadata> listFolder(String path) throws DbxException {
    ListFolderResult result = client.files().listFolder(path);
    List<Metadata> listFolder = new ArrayList<>();

    while (true) {
      listFolder.addAll(result.getEntries());
      if (!result.getHasMore()) {
        break;
      }
      result = client.files().listFolderContinue(result.getCursor());
    }
    return listFolder;
  }
}
