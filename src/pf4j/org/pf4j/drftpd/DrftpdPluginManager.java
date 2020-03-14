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

import java.nio.file.Paths;

import java.util.Arrays;
import java.util.List;

import org.pf4j.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drftpd implementation of the {@link PluginManager} interface.
 *
 * @author mikevg
 */
public class DrftpdPluginManager extends AbstractPluginManager {

    private static final Logger log = LoggerFactory.getLogger(DrftpdPluginManager.class);

    public static final String PLUGINS_PATHS_PROPERTY_NAME = "pf4j.pluginsPaths";

    private List<String> paths;

    public DrftpdPluginManager() {
        super();
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new ManifestPluginDescriptorFinder();
    }

    @Override
    protected ExtensionFinder createExtensionFinder() {
        DefaultExtensionFinder extensionFinder = new DefaultExtensionFinder(this);
        addPluginStateListener(extensionFinder);

        return extensionFinder;
    }

    @Override
    protected PluginFactory createPluginFactory() {
        return new DefaultPluginFactory();
    }

    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new DefaultExtensionFactory();
    }

    @Override
    protected PluginStatusProvider createPluginStatusProvider() {
        return new DrftpdPluginStatusProvider();
    }

    @Override
    protected PluginRepository createPluginRepository() {
        String configPaths = System.getProperty(PLUGINS_PATHS_PROPERTY_NAME);
        if (configPaths != null) {
            log.debug("Property ["+PLUGINS_PATHS_PROPERTY_NAME+"] found with value: ["+configPaths+"]");
            paths = Arrays.asList(configPaths.split("\\s*,\\s*"));
        }
        log.debug("paths now holds ["+paths.size()+"] items");

        if (paths == null || paths.size() < 1) {
            throw new RuntimeException("Command line property ["+PLUGINS_PATHS_PROPERTY_NAME+"] not provided");
        }

        CompoundPluginRepository repo = new CompoundPluginRepository();
        for (int i = 0; i < paths.size(); i++) {
            repo.add(new JarPluginRepository(Paths.get(paths.get(i))));
        }
        return repo;
    }

    @Override
    protected PluginLoader createPluginLoader() {
        return new JarPluginLoader(this);
    }

    @Override
    protected VersionManager createVersionManager() {
        return new DefaultVersionManager();
    }

    @Override
    protected void initialize() {
        super.initialize();

        log.info("PF4J version {} in '{}' mode", getVersion(), getRuntimeMode());
    }

}
