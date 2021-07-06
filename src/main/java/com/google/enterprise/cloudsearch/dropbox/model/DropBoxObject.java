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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Payload object for saving per document / item state. */
public final class DropBoxObject extends GenericJson {
  /** Log output */
  private static final Logger log = Logger.getLogger(DropBoxObject.class.getName());
  /** JSON Factory */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  /** Member ID prefix */
  private static final String MEMBER_ID_PREFIX = "dbmid:";

  public static final String MEMBER = "member";
  public static final String FOLDER = "folder";
  public static final String FILE = "file";

  /** DropBox object type */
  private static final Set<String> SUPPORTED_TYPE = ImmutableSet.of(MEMBER, FOLDER, FILE);

  @Key
  private String objectType;
  @Key
  private String teamMemberId;
  @Key
  private String memberDisplayName;
  @Key
  private String id;
  @Key
  private String name;
  @Key
  private String pathDisplay;
  @Key
  private String pathLower;
  @Key
  private String parentSharedFolderId;
  @Key
  private String sharedFolderId;
  @Key
  private Date clientModified;
  @Key
  private String contentHash;
  @Key
  private Boolean hasExplicitSharedMembers;
  @Key
  private boolean isDownloadable;
  @Key
  private String rev;
  @Key
  private Date serverModified;
  @Key
  private long size;

  /** Default constructor for json parsing. */
  public DropBoxObject() {
    super();
    setFactory(JSON_FACTORY);
  }

  /** Gets an instance of {@link DropBoxObject}. */
  private DropBoxObject(Builder builder) {
    this.objectType = builder.objectType;
    this.teamMemberId = builder.teamMemberId;
    this.memberDisplayName = builder.memberDisplayName;
    this.id = builder.id;
    this.name = builder.name;
    this.pathDisplay = builder.pathDisplay;
    this.pathLower = builder.pathLower;
    this.parentSharedFolderId = builder.parentSharedFolderId;
    this.sharedFolderId = builder.sharedFolderId;
    this.clientModified = builder.clientModified;
    this.contentHash = builder.contentHash;
    this.hasExplicitSharedMembers = builder.hasExplicitSharedMembers;
    this.isDownloadable = builder.isDownloadable;
    this.rev = builder.rev;
    this.size = builder.size;
    this.serverModified = builder.serverModified;
    setFactory(JSON_FACTORY);
  }

  /**
   * Decodes payload of the argument and gets an instance of {@link DropBoxObject}.
   *
   * @param payload payload to be decoded. This must be an encoded {@link DropBoxObject} instance
   * @return an instance of {@link DropBoxObject}
   * @throws IOException when decoding payload fails
   */
  public static DropBoxObject decodePayload(byte[] payload) throws IOException {
    return parse(new String(payload, UTF_8));
  }

  /**
   * Gets an instance of {@link DropBoxObject} from JSON string.
   *
   * @param payloadString JSON string
   * @return an instance of {@link DropBoxObject}
   * @throws IOException when getting an instance of {@link DropBoxObject} fails
   */
  private static DropBoxObject parse(String payloadString) throws IOException {
    log.log(Level.FINE, "Parsing {0}", payloadString);
    return JSON_FACTORY.fromString(payloadString, DropBoxObject.class);
  }

  /**
   * Encodes the contents of the {@link DropBoxObject} instance.
   *
   * @return encoded {@link DropBoxObject} instance
   * @throws IOException when encoding {@link DropBoxObject} instance fails
   */
  public byte[] encodePayload() throws IOException {
    return this.toPrettyString().getBytes(UTF_8);
  }

  /**
   * Validates an instance of {@link DropBoxObject}.
   *
   * @return {@code true} if an instance of {@link DropBoxObject} is correct
   */
  public boolean isValid() {
    if (!teamMemberId.startsWith(MEMBER_ID_PREFIX)
        || !SUPPORTED_TYPE.contains(objectType)
        || Strings.isNullOrEmpty(memberDisplayName)) {
      return false;
    }

    if (objectType.equals(MEMBER)) {
      return true;
    }

    List<String> required = new ArrayList<>(Arrays.asList(id, name, pathDisplay, pathLower));
    if (objectType.equals(FILE)) {
      if (clientModified == null || serverModified == null) {
        return false;
      }
      required.addAll(Arrays.asList(contentHash, rev));
    }

    for (String value : required) {
      if (Strings.isNullOrEmpty(value)) {
        return false;
      }
    }
    return true;
  }

  /** Gets dropBox object type. */
  public String getObjectType() {
    return objectType;
  }

  /** Gets team member ID. */
  public String getTeamMemberId() {
    return teamMemberId;
  }

  /** Gets member display name. */
  public String getMemberDisplayName() {
    return memberDisplayName;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getPathDisplay() {
    return pathDisplay;
  }

  public String getPathLower() {
    return pathLower;
  }

  public String getParentSharedFolderId() {
    return parentSharedFolderId;
  }

  public String getSharedFolderId() {
    return sharedFolderId;
  }

  public Date getClientModified() {
    return clientModified;
  }

  public String getContentHash() {
    return contentHash;
  }

  public Boolean getHasExplicitSharedMembers() {
    return hasExplicitSharedMembers;
  }

  public boolean getIsDownloadable() {
    return isDownloadable;
  }

  public String getRev() {
    return rev;
  }

  public Date getServerModified() {
    return serverModified;
  }

  public long getSize() {
    return size;
  }

  @Override
  public String toString() {
    return super.toString();
  }

  /** Builder object for creating an instance of {@link DropBoxObject}. */
  public static final class Builder {
    /** DropBox object type */
    private String objectType;
    /** Team member ID */
    private String teamMemberId;
    /** Member display name */
    private String memberDisplayName;
    private String id = "";
    private String name = "";
    private String pathDisplay = "";
    private String pathLower = "";
    private String parentSharedFolderId = "";
    private String sharedFolderId = "";
    private Date clientModified = null;
    private String contentHash = "";
    private Boolean hasExplicitSharedMembers = null;
    private boolean isDownloadable = false;
    private String rev = "";
    private Date serverModified = null;
    private long size = 0L;

    /**
     * Constructs a {@link DropBoxObject.Builder} that wraps given DropBox object type, team
     * member ID and member display name.
     *
     * @param objectType        dropBox object type
     * @param teamMemberId      team member ID
     * @param memberDisplayName member display name
     */
    public Builder(String objectType, String teamMemberId, String memberDisplayName) {
      this.objectType = objectType;
      this.teamMemberId = teamMemberId;
      this.memberDisplayName = memberDisplayName;
    }

    public Builder setId(String id) {
      this.id = id;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setPathDisplay(String pathDisplay) {
      this.pathDisplay = pathDisplay;
      return this;
    }

    public Builder setPathLower(String pathLower) {
      this.pathLower = pathLower;
      return this;
    }

    public Builder setParentSharedFolderId(String parentSharedFolderId) {
      this.parentSharedFolderId = Strings.nullToEmpty(parentSharedFolderId);
      return this;
    }

    public Builder setSharedFolderId(String sharedFolderId) {
      this.sharedFolderId = Strings.nullToEmpty(sharedFolderId);
      return this;
    }

    public Builder setClientModified(Date clientModified) {
      this.clientModified = clientModified;
      return this;
    }

    public Builder setContentHash(String contentHash) {
      this.contentHash = contentHash;
      return this;
    }

    public Builder setHasExplicitSharedMembers(Boolean hasExplicitSharedMembers) {
      this.hasExplicitSharedMembers = hasExplicitSharedMembers;
      return this;
    }

    public Builder setDownloadable(boolean isDownloadable) {
      this.isDownloadable = isDownloadable;
      return this;
    }

    public Builder setRev(String rev) {
      this.rev = rev;
      return this;
    }

    public Builder setServerModified(Date serverModified) {
      this.serverModified = serverModified;
      return this;
    }

    public Builder setSize(long size) {
      this.size = size;
      return this;
    }

    /**
     * Builds an instance of {@link DropBoxObject}.
     *
     * @return an instance of {@link DropBoxObject}
     */
    public DropBoxObject build() {
      return new DropBoxObject(this);
    }
  }
}
