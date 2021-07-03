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
