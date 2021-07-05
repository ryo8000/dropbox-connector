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
package com.google.enterprise.cloudsearch.dropbox.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class PathTest {
  @Test
  public void testCreatePath() {
    assertEquals("path", Path.createPath(Arrays.asList("path")));
    assertEquals("path/to/file", Path.createPath(Arrays.asList("path", "to", "file")));
    assertEquals("path/to/file", Path.createPath(Arrays.asList("path/to", "/file")));
  }
}
