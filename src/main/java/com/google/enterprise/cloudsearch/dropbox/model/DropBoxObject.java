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
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DropBoxObject extends GenericJson {
  /** Log output */
  private static final Logger log = Logger.getLogger(DropBoxObject.class.getName());
  /** JSON Factory */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static final String MEMBER_ID_PREFIX = "dbmid:";

  public static final String MEMBER = "member";
  public static final String FOLDER = "folder";
  public static final String FILE = "file";

  /** DropBox object type */
  private static final Set<String> SUPPORTED_TYPE = ImmutableSet.of(MEMBER);

  @Key
  private String objectType;
  @Key
  private String teamMemberId;

  /** Default constructor for json parsing. */
  public DropBoxObject() {
    super();
    setFactory(JSON_FACTORY);
  }

  /** Gets an instance of {@link DropBoxObject}. */
  public DropBoxObject(Builder builder) {
    this.objectType = builder.objectType;
    this.teamMemberId = builder.teamMemberId;
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
    return teamMemberId.startsWith(MEMBER_ID_PREFIX) && SUPPORTED_TYPE.contains(objectType);
  }

  /** Gets dropBox object type. */
  public String getObjectType() {
    return objectType;
  }

  /** Gets team member ID. */
  public String getTeamMemberId() {
    return teamMemberId;
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

    /**
     * Constructs a {@link DropBoxObject.Builder} that wraps given DropBox object type and team
     * member ID.
     *
     * @param objectType   dropBox object type
     * @param teamMemberId team member ID
     */
    public Builder(String objectType, String teamMemberId) {
      this.objectType = objectType;
      this.teamMemberId = teamMemberId;
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
