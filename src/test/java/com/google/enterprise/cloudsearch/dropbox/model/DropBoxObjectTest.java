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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.Test;

public class DropBoxObjectTest {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final Date NOW = now();

  @Test
  public void testBuilder() throws Exception {
    DropBoxObject member = new DropBoxObject.Builder(
        DropBoxObject.MEMBER, "dbmid:ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789", "my name").build();
    assertTrue(member.isValid());
    validateParseAndEquals(member);

    DropBoxObject folder1 = new DropBoxObject.Builder(
        DropBoxObject.FOLDER, "dbmid:ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789", "my name")
            .setName("my-folder")
            .setPathDisplay("/my-folder")
            .setSharedFolderId(null)
            .build();
    assertTrue(folder1.isValid());
    validateParseAndEquals(folder1);
    assertEquals("my-folder", folder1.getName());
    assertEquals("/my-folder", folder1.getPathDisplay());
    assertEquals("", folder1.getSharedFolderId());

    DropBoxObject folder2 = new DropBoxObject.Builder(
        DropBoxObject.FOLDER, "dbmid:ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789", "my name")
            .setName("my-folder")
            .setPathDisplay("/my-folder")
            .setSharedFolderId("1234567890")
            .build();
    assertTrue(folder2.isValid());
    validateParseAndEquals(folder2);
    assertEquals("1234567890", folder2.getSharedFolderId());

    DropBoxObject file1 = new DropBoxObject.Builder(
        DropBoxObject.FOLDER, "dbmid:ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789", "my name")
            .setName("my-file.txt")
            .setPathDisplay("/my-file.txt")
            .setDownloadable(true)
            .setServerModified(NOW)
            .build();
    assertTrue(file1.isValid());
    // validateParseAndEquals(file1);
    assertEquals("my-file.txt", file1.getName());
    assertEquals("/my-file.txt", file1.getPathDisplay());
    assertEquals(true, file1.getIsDownloadable());
    assertEquals(NOW, file1.getServerModified());
  }

  @Test
  public void testMissingValues() {
    DropBoxObject member1 = new DropBoxObject.Builder(
        DropBoxObject.MEMBER, "dbid:ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", "my name").build();
    assertFalse(member1.isValid());

    DropBoxObject folder1 = new DropBoxObject.Builder(
        DropBoxObject.FOLDER, "dbmid:ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789", "my name")
            .setName("my-folder")
            .setPathDisplay("")
            .setSharedFolderId(null)
            .build();
    assertFalse(folder1.isValid());

    DropBoxObject file1 = new DropBoxObject.Builder(
        DropBoxObject.FILE, "dbmid:ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789", "my name")
            .setName("my-file.txt")
            .setPathDisplay("/my-file.txt")
            .setDownloadable(true)
            .setServerModified(null)
            .build();
    assertFalse(file1.isValid());
  }

  public void testParsing() throws IOException {
    GenericJson toParse = new GenericJson();
    toParse.setFactory(JSON_FACTORY);
    toParse.put("objectType", "file");
    toParse.put("teamMemberId", "dbmid:ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789");
    toParse.put("memberDisplayName", "my name");
    toParse.put("name", "my-file.txt");
    toParse.put("pathDisplay", "/my-file.txt");
    toParse.put("sharedFolderId", "0123456789");
    toParse.put("isDownloadable", true);
    toParse.put("serverModified", NOW);
    byte[] encoded = toParse.toPrettyString().getBytes();
    DropBoxObject decoded = DropBoxObject.decodePayload(encoded);
    assertTrue(decoded.isValid());
    assertEquals("file", decoded.getObjectType());
    assertEquals("dbmid:ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789", decoded.getTeamMemberId());
    assertEquals("my name", decoded.getMemberDisplayName());
    assertEquals("my-file.txt", decoded.getName());
    assertEquals("/my-file.txt", decoded.getPathDisplay());
    assertEquals("0123456789", decoded.getSharedFolderId());
    assertEquals(true, decoded.getIsDownloadable());
    assertEquals(NOW, decoded.getServerModified());
  }

  private static Date now() {
    String strDate = "2021-01-01";
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date date = null;
    try {
      date = dateFormat.parse(strDate);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return date;
  }

  private void validateParseAndEquals(DropBoxObject object) throws IOException {
    byte[] encoded = object.encodePayload();
    DropBoxObject decoded = DropBoxObject.decodePayload(encoded);
    assertEquals(object, decoded);
  }
}
