package com.google.enterprise.cloudsearch.dropbox.util;

import java.util.List;

/** Utility class for operate path. */
public class Path {

  private Path() {
  }

  /**
   * Creates a path by joining a list of paths.
   *
   * @param paths a list of paths
   * @return joined path
   */
  public static String createPath(List<String> paths) {
    String joinedPath = String.join("/", paths);
    return joinedPath.replace("//", "/");
  }
}
