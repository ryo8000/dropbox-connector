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

import com.google.enterprise.cloudsearch.sdk.InvalidConfigurationException;
import com.google.enterprise.cloudsearch.sdk.config.Configuration.ResetConfigRule;
import com.google.enterprise.cloudsearch.sdk.config.Configuration.SetupConfigRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DropBoxConfigurationTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();
  @Rule
  public ResetConfigRule resetConfig = new ResetConfigRule();
  @Rule
  public SetupConfigRule setupConfig = SetupConfigRule.uninitialized();

  @Test
  public void testFromConfigurationConfigNotInitialized() {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("configuration not initialized");
    DropBoxConfiguration.fromConfiguration();
  }

  @Test
  public void testFromConfigurationConfigNoCredentialFile() {
    setupConfig.initConfig(new Properties());
    thrown.expect(InvalidConfigurationException.class);
    thrown.expectMessage("credentialFile can not be empty");
    DropBoxConfiguration.fromConfiguration();
  }

  @Test
  public void testFromConfigurationWithDefaults() {
    setupConfig.initConfig(getBaseConfiguration());
    DropBoxConfiguration configuration = DropBoxConfiguration.fromConfiguration();
    assertEquals("path/to/file", configuration.getCredentialFile());
    assertEquals(Collections.emptyList(), configuration.getTeamMemberIds());
  }

  @Test
  public void testFromConfigurationWithNonDefaults() throws Exception {
    Properties baseConfiguration = getBaseConfiguration();
    baseConfiguration.put("dropbox.teamMemberIds", "dbmid:ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789");
    setupConfig.initConfig(baseConfiguration);
    DropBoxConfiguration configuration = DropBoxConfiguration.fromConfiguration();
    assertEquals("path/to/file", configuration.getCredentialFile());
    assertEquals(Arrays.asList("dbmid:ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789"),
        configuration.getTeamMemberIds());
  }

  private Properties getBaseConfiguration() {
    Properties properties = new Properties();
    properties.put("dropbox.credentialFile", "path/to/file");
    return properties;
  }
}
