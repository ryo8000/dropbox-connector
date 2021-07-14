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

/** Utility class for operate path. */
public class Path {

  private Path() {
  }

  /**
   * Creates a path by joining a list of paths.
   *
   * @param firstPath      first path
   * @param remainingPaths a list of paths
   * @return joined path
   */
  public static String createPath(String firstPath, String... remainingPaths) {
    String joinedPath = String.join("/", remainingPaths);
    if (!joinedPath.isEmpty()) {
      joinedPath = "/" + joinedPath;
    }
    return (firstPath + joinedPath).replace("//", "/");
  }
}
