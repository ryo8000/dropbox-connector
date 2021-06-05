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
package com.google.enterprise.cloudsearch.dropbox.contents;

import static com.google.common.base.Preconditions.checkState;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.team.TeamMemberInfo;
import com.google.api.services.cloudsearch.v1.model.Item;
import com.google.api.services.cloudsearch.v1.model.PushItem;
import com.google.enterprise.cloudsearch.dropbox.DropBoxConfiguration;
import com.google.enterprise.cloudsearch.dropbox.client.DropBoxClientFactory;
import com.google.enterprise.cloudsearch.dropbox.client.TeamClient;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterable;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterableImpl;
import com.google.enterprise.cloudsearch.sdk.RepositoryException;
import com.google.enterprise.cloudsearch.sdk.indexing.template.ApiOperation;
import com.google.enterprise.cloudsearch.sdk.indexing.template.PushItems;
import com.google.enterprise.cloudsearch.sdk.indexing.template.Repository;
import com.google.enterprise.cloudsearch.sdk.indexing.template.RepositoryContext;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/** Repository implementation for indexing content from DropBox repository. */
final class DropBoxRepository implements Repository {
  /** Log output */
  private static final Logger log = Logger.getLogger(DropBoxRepository.class.getName());

  private TeamClient teamClient;

  DropBoxRepository() {
  }

  /**
   * Initializes the connection to DropBox as well as the list of repositories to index.
   *
   * @param context {@link RepositoryContext}.
   * @throws RepositoryException when repository initialization fails.
   */
  @Override
  public void init(RepositoryContext repositoryContext) throws RepositoryException {
    DropBoxConfiguration dropBoxConfiguration = DropBoxConfiguration.fromConfiguration();
    teamClient = DropBoxClientFactory.getTeamClient(dropBoxConfiguration.getCredentialFile());
  }

  /**
   * Gets all of the existing user IDs from the data repository.
   *
   * <p>
   * Every user's data in the <em>repository</em> is pushed to the Cloud Search queue. Each pushed
   * data is later polled and processed in the {@link #getDoc(Item)} method.
   *
   * @param checkpoint value defined and maintained by this connector.
   * @return {@link CheckpointCloseableIterable} object containing list of {@link PushItem}.
   * @throws RepositoryException on data access errors.
   */
  @Override
  public CheckpointCloseableIterable<ApiOperation> getIds(byte[] checkpoint)
      throws RepositoryException {
    log.entering("DropBoxConnector", "getIds");
    PushItems.Builder pushItemsBuilder = new PushItems.Builder();

    try {
      List<TeamMemberInfo> members = teamClient.getMembers();

      for (TeamMemberInfo member : members) {
        // TODO

      }
    } catch (DbxException e) {
      throw new RepositoryException.Builder()
          .setErrorMessage("Failed to get user IDs")
          .setCause(e)
          .build();
    }

    ApiOperation pushItems = pushItemsBuilder.build();
    CheckpointCloseableIterable<ApiOperation> allIds =
        new CheckpointCloseableIterableImpl.Builder<>(Collections.singleton(pushItems)).build();
    log.exiting("DropBoxConnector", "getIds");
    return allIds;
  }

  @Override
  public CheckpointCloseableIterable<ApiOperation> getChanges(byte[] checkpoint)
      throws RepositoryException {
    return null;
  }

  @Override
  public ApiOperation getDoc(Item item) throws RepositoryException {
    // TODO
    return null;
  }

  @Override
  public CheckpointCloseableIterable<ApiOperation> getAllDocs(byte[] checkpoint) {
    return null;
  }

  @Override
  public boolean exists(Item item) {
    return false;
  }

  @Override
  public void close() {
  }
}
