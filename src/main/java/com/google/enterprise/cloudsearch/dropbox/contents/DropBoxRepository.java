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

import static com.google.enterprise.cloudsearch.sdk.indexing.IndexingItemBuilder.FieldOrValue.withValue;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.team.TeamMemberInfo;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.cloudsearch.v1.model.Item;
import com.google.api.services.cloudsearch.v1.model.Principal;
import com.google.api.services.cloudsearch.v1.model.PushItem;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.enterprise.cloudsearch.dropbox.client.DropBoxClientFactory;
import com.google.enterprise.cloudsearch.dropbox.client.MemberClient;
import com.google.enterprise.cloudsearch.dropbox.client.TeamClient;
import com.google.enterprise.cloudsearch.dropbox.model.DropBoxConfiguration;
import com.google.enterprise.cloudsearch.dropbox.model.DropBoxObject;
import com.google.enterprise.cloudsearch.dropbox.model.SharingInfo;
import com.google.enterprise.cloudsearch.dropbox.util.Path;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterable;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterableImpl;
import com.google.enterprise.cloudsearch.sdk.RepositoryException;
import com.google.enterprise.cloudsearch.sdk.indexing.Acl;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingItemBuilder;
import com.google.enterprise.cloudsearch.sdk.indexing.StructuredData;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingItemBuilder.ItemType;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingService.ContentFormat;
import com.google.enterprise.cloudsearch.sdk.indexing.template.ApiOperation;
import com.google.enterprise.cloudsearch.sdk.indexing.template.ApiOperations;
import com.google.enterprise.cloudsearch.sdk.indexing.template.PushItems;
import com.google.enterprise.cloudsearch.sdk.indexing.template.Repository;
import com.google.enterprise.cloudsearch.sdk.indexing.template.RepositoryContext;
import com.google.enterprise.cloudsearch.sdk.indexing.template.RepositoryDoc;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Repository implementation for indexing content from DropBox repository. */
final class DropBoxRepository implements Repository {
  /** Log output */
  private static final Logger log = Logger.getLogger(DropBoxRepository.class.getName());
  /** Logging message when a document is successfully created */
  private static final String SUCCESS_LOG = "item has been processed successfully [{0}]";
  /** Member's root path */
  private static final String ROOT_PATH = "";
  /** Root URL of Dropbox */
  private static final String ROOT_URL = "https://www.dropbox.com/home";

  /** Team client */
  private TeamClient teamClient;
  /** List of team member IDs to be processed */
  private List<String> teamMemberIds;

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
    teamMemberIds = dropBoxConfiguration.getTeamMemberIds();
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
    PushItems.Builder pushItemsBuilder = new PushItems.Builder();

    int totalMembers = 0;
    try {
      List<TeamMemberInfo> members = teamClient.getMembers();

      for (TeamMemberInfo member : members) {
        String teamMemberId = member.getProfile().getTeamMemberId();
        String memberName = member.getProfile().getName().getDisplayName();

        if (!(teamMemberIds.isEmpty() || teamMemberIds.contains(teamMemberId))) {
          log.log(Level.FINE, "skip member {0}", teamMemberId);
          continue;
        }
        totalMembers++;

        DropBoxObject dropBoxObject =
            new DropBoxObject.Builder(DropBoxObject.MEMBER, teamMemberId, memberName)
                .build();

        String url = Path.createPath(ROOT_URL, memberName);
        pushItemsBuilder.addPushItem(
            url, new PushItem().encodePayload(dropBoxObject.encodePayload()));
      }
    } catch (DbxException | IOException e) {
      throw new RepositoryException.Builder()
          .setErrorMessage("Failed to get user IDs")
          .setCause(e)
          .build();
    }

    ApiOperation pushItems = pushItemsBuilder.build();
    CheckpointCloseableIterable<ApiOperation> allIds =
        new CheckpointCloseableIterableImpl.Builder<>(Collections.singleton(pushItems)).build();
    log.log(Level.INFO,
        "process of get user IDs has been completed successfully. total member: {0}",
        totalMembers);
    return allIds;
  }

  /**
   * Gets all changed documents since the last traversal.
   *
   * @param checkpoint encoded checkpoint bytes.
   * @return {@link CheckpointCloseableIterable} object containing list of {@link ApiOperation} to
   *         execute with new traversal checkpoint value.
   * @throws RepositoryException when change detection fails.
   */
  @Override
  public CheckpointCloseableIterable<ApiOperation> getChanges(byte[] checkpoint)
      throws RepositoryException {
    // TODO
    return new CheckpointCloseableIterableImpl.Builder<ApiOperation>(
        Collections.emptyList())
            .setHasMore(false)
            .build();
  }

  /**
   * Gets a single data repository item and indexes it if required.
   *
   * <p>
   * This method typically returns a {@link RepositoryDoc} object corresponding to passed
   * {@link Item}. However, if the requested document is no longer in the data repository, then a
   * {@link DeleteItem} operation might be returned instead.
   *
   * @param item the data repository item to retrieve.
   * @return the item's state determines which type of {@link ApiOperation} is returned:
   *         {@link RepositoryDoc}, {@link DeleteItem}, or {@link PushItem}.
   * @throws RepositoryException when the processing of the item fails.
   */
  @Override
  public ApiOperation getDoc(Item item) throws RepositoryException {
    DropBoxObject dropBoxObject;
    try {
      dropBoxObject = DropBoxObject.decodePayload(item.decodePayload());
    } catch (IOException e) {
      log.log(Level.WARNING, String.format("Invalid DropBox payload Object on item %s", item), e);
      return ApiOperations.deleteItem(item.getName());
    }
    if (!dropBoxObject.isValid()) {
      log.log(Level.WARNING, "Invalid DropBox payload Object {0} on item {1}",
          new Object[] {dropBoxObject, item});
      return ApiOperations.deleteItem(item.getName());
    }

    MemberClient memberClient = teamClient.asMember(dropBoxObject.getTeamMemberId());

    try {
      switch (dropBoxObject.getObjectType()) {
        case DropBoxObject.MEMBER:
          return createMemberDoc(memberClient, item, dropBoxObject);
        case DropBoxObject.FOLDER:
          return createFolderDoc(memberClient, item, dropBoxObject);
        case DropBoxObject.FILE:
          return createFileDoc(memberClient, item, dropBoxObject);
        default:
          throw new RepositoryException.Builder()
              .setErrorMessage(String.format("Unexpected item received: [%s]", item.getName()))
              .setErrorType(RepositoryException.ErrorType.UNKNOWN)
              .build();
      }
    } catch (IOException e) {
      throw new RepositoryException.Builder()
          .setErrorMessage(String.format("Failed to process item: [%s]", item.getName()))
          .setCause(e)
          .build();
    }
  }

  /**
   * Not implemented by this repository.
   */
  @Override
  public CheckpointCloseableIterable<ApiOperation> getAllDocs(byte[] checkpoint) {
    return null;
  }

  /**
   * Not implemented by this repository.
   */
  @Override
  public boolean exists(Item item) {
    return false;
  }

  /**
   * Not implemented by this repository.
   */
  @Override
  public void close() {
    // Performs any data repository shut down code here.
  }

  /**
   * Create a document to index the member's root information.
   */
  private ApiOperation createMemberDoc(MemberClient memberClient, Item polledItem,
      DropBoxObject dropBoxObject) throws IOException {
    String polledItemName = polledItem.getName();
    String memberName = dropBoxObject.getMemberDisplayName();
    String teamMemberId = dropBoxObject.getTeamMemberId();

    // ACL
    List<Principal> users = Collections.singletonList(Acl.getUserPrincipal(teamMemberId));
    Acl acl = new Acl.Builder()
        .setReaders(users)
        .build();

    // build item
    IndexingItemBuilder itemBuilder = new IndexingItemBuilder(polledItemName)
        .setTitle(withValue(memberName))
        .setItemType(ItemType.CONTAINER_ITEM)
        .setAcl(acl)
        .setSourceRepositoryUrl(withValue(polledItemName))
        .setPayload(polledItem.decodePayload());
    if (StructuredData.hasObjectDefinition(dropBoxObject.getObjectType())) {
      itemBuilder.setObjectType(withValue(dropBoxObject.getObjectType()));
    }
    Item item = itemBuilder.build();

    RepositoryDoc.Builder docBuilder = new RepositoryDoc.Builder().setItem(item);

    // child items
    Map<String, PushItem> items = getChildItems(teamMemberId, memberName, memberClient, ROOT_PATH);
    items.entrySet().stream().forEach(e -> docBuilder.addChildId(e.getKey(), e.getValue()));

    RepositoryDoc document = docBuilder.build();
    log.log(Level.INFO, SUCCESS_LOG, polledItemName);
    return document;
  }

  /**
   * Create a document to index a folder.
   */
  private ApiOperation createFolderDoc(MemberClient memberClient, Item polledItem,
      DropBoxObject dropBoxObject) throws IOException {
    String polledItemName = polledItem.getName();
    String memberName = dropBoxObject.getMemberDisplayName();
    String teamMemberId = dropBoxObject.getTeamMemberId();
    String folderPath = dropBoxObject.getPathDisplay();
    String sharedFolderId = dropBoxObject.getSharedFolderId();

    // ACL
    List<Principal> permits;
    if (sharedFolderId.isEmpty()) {
      permits = Collections.singletonList(Acl.getUserPrincipal(teamMemberId));
    } else {
      SharingInfo sharingInfo;
      try {
        sharingInfo = memberClient.getFolderSharingInfo(sharedFolderId);
      } catch (DbxException e) {
        throw new IOException(e);
      }
      permits = createSharedReaders(sharingInfo);
    }
    Acl acl = new Acl.Builder()
        .setReaders(permits)
        .build();

    // build item
    IndexingItemBuilder itemBuilder = new IndexingItemBuilder(polledItemName)
        .setTitle(withValue(dropBoxObject.getName()))
        .setItemType(ItemType.CONTAINER_ITEM)
        .setAcl(acl)
        .setSourceRepositoryUrl(withValue(polledItemName))
        .setPayload(polledItem.decodePayload());
    if (StructuredData.hasObjectDefinition(dropBoxObject.getObjectType())) {
      itemBuilder.setObjectType(withValue(dropBoxObject.getObjectType()));
    }
    Item item = itemBuilder.build();

    RepositoryDoc.Builder docBuilder = new RepositoryDoc.Builder().setItem(item);

    // child items
    Map<String, PushItem> items = getChildItems(teamMemberId, memberName, memberClient, folderPath);
    items.entrySet().stream().forEach(e -> docBuilder.addChildId(e.getKey(), e.getValue()));

    RepositoryDoc document = docBuilder.build();
    log.log(Level.INFO, SUCCESS_LOG, polledItemName);
    return document;
  }

  /**
   * Create a document to index a file.
   */
  private ApiOperation createFileDoc(MemberClient memberClient, Item polledItem,
      DropBoxObject dropBoxObject) throws IOException {
    String polledItemName = polledItem.getName();
    String filePath = dropBoxObject.getPathDisplay();

    // File Content
    ByteArrayContent fileContent = null;
    if (dropBoxObject.getIsDownloadable()) {
      DbxDownloader<FileMetadata> file;
      try {
        file = memberClient.download(filePath);
      } catch (DbxException e) {
        throw new IOException(e);
      }
      try (InputStream contentStream = file.getInputStream()) {
        String mimeType = file.getContentType();
        fileContent = new ByteArrayContent(mimeType, ByteStreams.toByteArray(contentStream));
      }
    }

    // ACL
    SharingInfo sharingInfo;
    try {
      sharingInfo = memberClient.getFileSharingInfo(filePath);
    } catch (DbxException e) {
      throw new IOException(e);
    }
    List<Principal> permits = createSharedReaders(sharingInfo);
    Acl acl = new Acl.Builder()
        .setReaders(permits)
        .build();

    // build item
    IndexingItemBuilder itemBuilder = new IndexingItemBuilder(polledItemName)
        .setTitle(withValue(dropBoxObject.getName()))
        .setItemType(ItemType.CONTENT_ITEM)
        .setAcl(acl)
        .setSourceRepositoryUrl(withValue(polledItemName))
        .setPayload(polledItem.decodePayload())
        .setUpdateTime(withValue(new DateTime(dropBoxObject.getServerModified())));
    if (StructuredData.hasObjectDefinition(dropBoxObject.getObjectType())) {
      itemBuilder.setObjectType(withValue(dropBoxObject.getObjectType()));
    }
    Item item = itemBuilder.build();

    RepositoryDoc.Builder docBuilder = new RepositoryDoc.Builder()
        .setItem(item);

    if (fileContent != null) {
      docBuilder.setContent(fileContent, ContentFormat.RAW);
    }

    RepositoryDoc document = docBuilder.build();
    log.log(Level.INFO, SUCCESS_LOG, polledItemName);
    return document;
  }

  /**
   * Based on the sharing information, create a list of readable users and groups.
   */
  private List<Principal> createSharedReaders(SharingInfo sharingInfo) {
    List<Principal> readers = Lists.newArrayList();

    List<Principal> users = sharingInfo.getUserIds().stream()
        .map(Acl::getUserPrincipal)
        .collect(Collectors.toList());
    List<Principal> groups = sharingInfo.getGroupNames().stream()
        .map(Acl::getGroupPrincipal)
        .collect(Collectors.toList());

    readers.addAll(users);
    readers.addAll(groups);
    return readers;
  }

  /**
   * Get the child items under the item to be processed.
   * {@code path} argument should be the path to the item to be processed.
   */
  private Map<String, PushItem> getChildItems(String teamMemberId, String memberName,
      MemberClient memberClient, String path) throws IOException {
    Map<String, PushItem> items = new HashMap<>();

    List<Metadata> contents;
    try {
      contents = memberClient.listFolder(path);
    } catch (DbxException e) {
      throw new IOException(e);
    }

    for (Metadata content : contents) {
      DropBoxObject dropBoxObject = null;

      if (content instanceof FolderMetadata) {
        FolderMetadata folder = (FolderMetadata) content;
        dropBoxObject =
            new DropBoxObject.Builder(DropBoxObject.FOLDER, teamMemberId, memberName)
                .setName(folder.getName())
                .setPathDisplay(folder.getPathDisplay())
                .setSharedFolderId(folder.getSharedFolderId())
                .build();
      } else if (content instanceof FileMetadata) {
        FileMetadata file = (FileMetadata) content;
        dropBoxObject =
            new DropBoxObject.Builder(DropBoxObject.FILE, teamMemberId, memberName)
                .setName(file.getName())
                .setPathDisplay(file.getPathDisplay())
                .setDownloadable(file.getIsDownloadable())
                .setServerModified(file.getServerModified())
                .build();
      } else {
        continue;
      }

      String url = Path.createPath(ROOT_URL, memberName, dropBoxObject.getPathDisplay());
      items.put(url, new PushItem().encodePayload(dropBoxObject.encodePayload()));
    }
    return items;
  }
}
