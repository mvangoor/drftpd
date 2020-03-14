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
package org.drftpd.tools.installer.auto;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.drftpd.tools.installer.InstallerConfig;
import org.drftpd.tools.installer.PluginBuilder;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import java.io.IOException;
import java.io.PipedInputStream;

import java.util.ArrayList;
import java.util.HashMap;

public class AutoInstaller {

	private static final Logger logger = LogManager.getLogger(AutoInstaller.class);

	public AutoInstaller(PluginManager manager, InstallerConfig config, boolean cleanOnly) {

		ArrayList<PluginWrapper> toBuild = new ArrayList<>();

    HashMap<String,Boolean> pluginConfig = config.getPluginSelections();
		for (PluginWrapper plugin : manager.getPlugins()) {
  		Boolean sel = pluginConfig.get(plugin.getDescriptor().getPluginId());
      logger.fatal("["+plugin.getDescriptor().getPluginId()+" -> "+sel);
      // If we want to build all plugins, this is build phase -- TODO: Remove this
      if (false) {
        toBuild.add(plugin);
  			if (sel == null) {
          pluginConfig.put(plugin.getDescriptor().getPluginId(), Boolean.TRUE);
  			}
      } else {
        if (sel != null && sel) {
          toBuild.add(plugin);
        }
      }
		}
    config.setPluginSelections(pluginConfig);
    logger.debug("Current plugin configuration: [" + config.getPluginSelections() + "]");

		if (config.getConvertUsers()) {
			logger.warn("Converting dr2 user files is not supported in autobuild mode.");
			config.setConvertUsers(false);
		}

		PipedInputStream logInput = new PipedInputStream();
		LogWindow logWindow = new LogWindow(logInput, config, toBuild.size(), cleanOnly);
		PluginBuilder builder = new PluginBuilder(toBuild, manager, logInput, config, logWindow, cleanOnly);
		logWindow.setBuilder(builder);
		try {
			logWindow.init();
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
