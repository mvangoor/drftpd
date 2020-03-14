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

import java.util.ArrayList;
import java.util.List;

import org.pf4j.PluginStatusProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A boot class that start the an Application.
 *
 * @author mikevg
 */
public class DrftpdPluginStatusProvider implements PluginStatusProvider {

    private static final Logger log = LoggerFactory.getLogger(DrftpdPluginStatusProvider.class);

    private List<String> disabledPlugins;

    public DrftpdPluginStatusProvider() {
        // We initialize this as an empty list and this will be populated during run time
        disabledPlugins = new ArrayList<>();
    }

    @Override
    public boolean isPluginDisabled(String pluginId) {

        if (disabledPlugins.contains(pluginId)) {
            return true;
        }

        return false;
    }

    @Override
    public void disablePlugin(String pluginId) {
        if (isPluginDisabled(pluginId)) {
            // do nothing
            return;
        }

        disabledPlugins.add(pluginId);
    }

    @Override
    public void enablePlugin(String pluginId) {
        if (!isPluginDisabled(pluginId)) {
            // do nothing
            return;
        }

        disabledPlugins.remove(pluginId);
    }
}
