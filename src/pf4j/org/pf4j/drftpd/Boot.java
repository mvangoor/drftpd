/*
 * This file is part of DrFTPD, Distributed FTP Daemon.
 *
 * DrFTPD is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrFTPD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DrFTPD; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pf4j.drftpd;

import java.util.List;
import java.util.Set;

import org.pf4j.PluginWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A boot class that start the an Application.
 *
 * @author mikevg
 */
public class Boot {
  private static final Logger log = LoggerFactory.getLogger(Boot.class);

  public static void main(String[] args) throws Exception {
    final DrftpdPluginManager pluginManager = new DrftpdPluginManager();

    // load the plugins
    pluginManager.loadPlugins();

    // start (active/resolved) the plugins
    pluginManager.startPlugins();

/*
TODO: Remove this (debugging)
    log.error("Extensions added by classpath:");
    Set<String> extensionClassNames = pluginManager.getExtensionClassNames(null);
    for (String extension : extensionClassNames) {
      log.error("   " + extension);
    }
    log.error("");

    log.error("Extension classes by classpath:");
    List<Class<? extends Application>> greetingsClasses = pluginManager.getExtensionClasses(Application.class);
    for (Class<? extends Application> greeting : greetingsClasses) {
      log.error("   Class: " + greeting.getCanonicalName());
    }
    log.error("");

    // print extensions ids for each started plugin
    List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
    for (PluginWrapper plugin : startedPlugins) {
      String pluginId = plugin.getDescriptor().getPluginId();
      System.out.println(String.format("Extensions added by plugin '%s':", pluginId));
      extensionClassNames = pluginManager.getExtensionClassNames(pluginId);
      for (String extension : extensionClassNames) {
        System.out.println("   " + extension);
      }
    }
*/

    // Get the Application extensions
    List<Application> apps = pluginManager.getExtensions(Application.class);
    log.error("Found ["+apps.size()+"] Application instances");
    if (apps.size() != 1) {
      throw new RuntimeException("I expect to only find one loaded plugin for Application Extension");
    }
    apps.get(0).start(pluginManager);
  }
}
