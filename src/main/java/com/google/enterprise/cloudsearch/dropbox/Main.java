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
package com.google.enterprise.cloudsearch.dropbox;

import com.google.enterprise.cloudsearch.dropbox.contents.DropBoxConnector;
import com.google.enterprise.cloudsearch.dropbox.identity.DropBoxIdentityConnector;

/**
 * Runs DropBox or identity connector based on command-line argument. With no
 * argument, runs the dropbox connector.
 *
 * Usage:
 *
 * <pre>
 * java -jar connector.jar --dropbox
 * java -jar connector.jar --identity
 * </pre>
 */
public final class Main {
  public static void main(String[] args) throws InterruptedException {
    boolean dropbox = false;
    boolean identity = false;
    for (String arg : args) {
      if (arg.equals("--dropbox")) {
        dropbox = true;
      } else if (arg.equals("--identity")) {
        identity = true;
      }
    }
    if (dropbox && identity) {
      System.out.println(
          "Invalid options; only one of --dropbox and --identity may be specified");
      return;
    }
    if (identity) {
      DropBoxIdentityConnector.main(args);
    } else {
      DropBoxConnector.main(args);
    }
  }
}
