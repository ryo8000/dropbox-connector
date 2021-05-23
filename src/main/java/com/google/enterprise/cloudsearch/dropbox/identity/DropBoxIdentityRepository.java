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
package com.google.enterprise.cloudsearch.dropbox.identity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.enterprise.cloudsearch.dropbox.DropBoxConfiguration;
import com.google.enterprise.cloudsearch.dropbox.client.DropBoxClient;
import com.google.enterprise.cloudsearch.dropbox.client.DropBoxClientFactory;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterable;
import com.google.enterprise.cloudsearch.sdk.identity.IdentityGroup;
import com.google.enterprise.cloudsearch.sdk.identity.IdentityUser;
import com.google.enterprise.cloudsearch.sdk.identity.Repository;
import com.google.enterprise.cloudsearch.sdk.identity.RepositoryContext;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DropBoxIdentityRepository implements Repository {
  private static final Logger log = Logger.getLogger(DropBoxIdentityRepository.class.getName());

  private RepositoryContext repositoryContext;
  private DropBoxClient client;

  DropBoxIdentityRepository() {
  }

  @Override
  public void init(RepositoryContext context) throws IOException {
    repositoryContext = checkNotNull(context, "repository context can not be null");
    DropBoxConfiguration dropBoxConfiguration = DropBoxConfiguration.fromConfiguration();
    client = DropBoxClientFactory.getTeamClient(dropBoxConfiguration.getCredentialFile());
  }

  @Override
  public CheckpointCloseableIterable<IdentityUser> listUsers(byte[] checkpoint) throws IOException {
    // TODO
    return null;
  }

  @Override
  public CheckpointCloseableIterable<IdentityGroup> listGroups(byte[] checkpoint)
      throws IOException {
    // TODO
    return null;
  }

  @Override
  public void close() {
  }
}
