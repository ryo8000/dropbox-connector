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

import java.util.Collections;
import java.util.List;

public class SharingInfo {
  private final List<String> userIds;
  private final List<String> groupNames;

  private SharingInfo(Builder builder) {
    this.userIds = Collections.unmodifiableList(builder.userIds);
    this.groupNames = Collections.unmodifiableList(builder.groupNames);
  }

  public List<String> getUserIds() {
    return userIds;
  }

  public List<String> getGroupNames() {
    return groupNames;
  }

  public static final class Builder {
    private List<String> userIds;
    private List<String> groupNames;

    public Builder(List<String> userIds, List<String> groupNames) {
      this.userIds = userIds;
      this.groupNames = groupNames;
    }

    public SharingInfo build() {
      return new SharingInfo(this);
    }
  }
}
