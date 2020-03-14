/*
 * Copyright 2012 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drftpd.tools.installer;

import java.io.IOException;

import java.util.List;
import java.util.Set;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import org.drftpd.tools.installer.auto.AutoInstaller;

/**
 * A boot class that start the demo.
 *
 * @author mikevg
 */
public class BuildSystem {

  private static final Logger logger = LogManager.getLogger(BuildSystem.class);

  public static void main(String[] args) throws IOException {

    // Load the build config
    ConfigReader cr = new ConfigReader();
    InstallerConfig config = cr.getConfig();

    // create the plugin manager
    Path plugin_path = Paths.get("src/core");
    final PluginManager pluginManager = new DefaultPluginManager(plugin_path);

    // load the plugins
    pluginManager.loadPlugins();

    // Get the resolved plugins
    List<PluginWrapper> plugins = pluginManager.getPlugins();
    logger.debug("Found [" + plugins.size() + "] plugins in plugindirectory: " + pluginManager.getPluginsRoot());

    // Only auto installer currently, other options might follow in the future
    AutoInstaller installer = new AutoInstaller(pluginManager, config, false);

    config.writeToDisk();
    logger.info("Finished");
  }
}
