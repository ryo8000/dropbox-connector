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

import com.google.api.services.cloudsearch.v1.model.Item;
import com.google.enterprise.cloudsearch.dropbox.DropBoxClient;
import com.google.enterprise.cloudsearch.dropbox.DropBoxClientFactory;
import com.google.enterprise.cloudsearch.dropbox.DropBoxConfiguration;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterable;
import com.google.enterprise.cloudsearch.sdk.RepositoryException;
import com.google.enterprise.cloudsearch.sdk.indexing.template.ApiOperation;
import com.google.enterprise.cloudsearch.sdk.indexing.template.Repository;
import com.google.enterprise.cloudsearch.sdk.indexing.template.RepositoryContext;

import java.util.logging.Level;
import java.util.logging.Logger;

final class DropBoxRepository implements Repository {
  private static final Logger log = Logger.getLogger(DropBoxRepository.class.getName());

  private DropBoxClient client;

  DropBoxRepository() {
  }

  @Override
  public void init(RepositoryContext repositoryContext) throws RepositoryException {
    DropBoxConfiguration dropBoxConfiguration = DropBoxConfiguration.fromConfiguration();
    client = DropBoxClientFactory.getTeamClient(dropBoxConfiguration.getCredentialFile());
  }

  @Override
  public CheckpointCloseableIterable<ApiOperation> getIds(byte[] checkpoint)
      throws RepositoryException {
    // TODO
    return null;
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
