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
