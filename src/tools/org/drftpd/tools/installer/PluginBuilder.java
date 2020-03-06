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
package org.drftpd.tools.installer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.taskdefs.Ant.TargetElement;
import org.apache.tools.ant.taskdefs.SubAnt;
import org.apache.tools.ant.types.FileList;

import org.pf4j.DefaultVersionManager;
import org.pf4j.DependencyResolver;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.nio.file.Path;

/**
 * @author djb61
 * @version $Id$
 */
public class PluginBuilder {

	private static final Logger logger = LogManager.getLogger(PluginBuilder.class);
	private SubAnt _antBuilder = new SubAnt();
	private PluginBuildListener _pbListener;
	private Project _builderProject;
	private boolean _cleanOnly;

	public PluginBuilder(ArrayList<PluginWrapper> toBuild, PluginManager manager, PipedInputStream logInput, InstallerConfig config, LogWindowInterface logWindow, boolean cleanOnly) {

		_cleanOnly = cleanOnly;

		// Sort selected plugins into correct order for building
		List<PluginDescriptor> descriptors = new ArrayList<>();
		for (PluginWrapper pw : toBuild) {
			descriptors.add(pw.getDescriptor());
		}
		DependencyResolver dr = new DependencyResolver(new DefaultVersionManager ());
		DependencyResolver.Result result = dr.resolve(descriptors);
		List<String> sortedPlugins = result.getSortedPlugins();

		// Create a list of build files for the selected plugins
		StringBuilder buildFiles = new StringBuilder();

		// Get the build.xml paths in order
		for (String pluginId : sortedPlugins) {
			PluginWrapper pw = null;
			for (PluginWrapper pw2 : toBuild) {
				if (pluginId.equals(pw2.getPluginId())) {
					pw = pw2;
					break;
				}
			}
			if (buildFiles.length() != 0) {
				buildFiles.append(",");
			}
			Path pluginFile = pw.getPluginPath().normalize();
			logger.debug("Plugin path: [" + pluginFile.resolve("build.xml").toString() + "], working path: [" + System.getProperty("user.dir") + "]");
			buildFiles.append(pluginFile.resolve("build.xml").toString());
		}
		logger.debug("List of buildFiles: [" + buildFiles.toString() + "]");

		// Set list of build files in the ant builder
		FileList fileList = new FileList();
		fileList.setFiles(buildFiles.toString());
		_antBuilder.addFilelist(fileList);

		// Create an ant Project and initialize default tasks/types
		_builderProject = new Project();
		_builderProject.init();

		// Read custom project wide config data and configure ant Project to use it
		File setupFile = new File(System.getProperty("user.dir")+File.separator+"setup.xml");
		logger.debug("Setup file: [" + setupFile + "]");
		ProjectHelper.configureProject(_builderProject, setupFile);

		// Add a custom build listener for logging and handling our additional needs
		_pbListener = new PluginBuildListener(logInput, config, toBuild, manager, logWindow, _cleanOnly);
		try {
			_pbListener.init();
		} catch (IOException e) {
			System.out.println(e);
		}
		_builderProject.addBuildListener(_pbListener);

		// Set installation dir if required
		if (!config.getInstallDir().equals("")) {
			_builderProject.setProperty("installdir", config.getInstallDir());
		} else {
			_builderProject.setProperty("installdir", System.getProperty("user.dir"));
		}

		// Set dev mode
		if (config.getDevMode()) {
			_builderProject.setProperty("devmode", "true");
		} else {
			_builderProject.setProperty("devmode", "false");
		}

		// Set target(s)
		if (config.getClean() || _cleanOnly) {
			TargetElement cleanTarget = new TargetElement();
			cleanTarget.setName("clean");
			_antBuilder.addConfiguredTarget(cleanTarget);
		}
		if (!_cleanOnly) {
			TargetElement buildTarget = new TargetElement();
			buildTarget.setName("build");
			_antBuilder.addConfiguredTarget(buildTarget);
		}
		
		// Set root dir of the build
		_builderProject.setProperty("buildroot", System.getProperty("user.dir"));

		// Final setup of ant builder
		_antBuilder.setProject(_builderProject);
		_antBuilder.setInheritall(true);
		_antBuilder.setFailonerror(false);
		_antBuilder.setVerbose(true);
    logger.fatal("buildPath: ["+_antBuilder.createBuildpath()+"]");
	}

	public void buildPlugins() {
		BuildException be = null;
		try {
			BuildEvent startEvent = new BuildEvent(_builderProject);
			if (_cleanOnly) {
				startEvent.setMessage("CLEAN STARTED",0);
			} else {
				startEvent.setMessage("BUILD STARTED",0);
			}
			_pbListener.buildStarted(startEvent);
      logger.fatal("pre execute");
			_antBuilder.execute();
      logger.fatal("post execute");
		} catch (BuildException e) {
      logger.fatal("Exception: ["+e+"]");
			be = e;
		} finally {
      logger.fatal("Finally");
			BuildEvent endEvent = new BuildEvent(_builderProject);
			if (be != null) {
				endEvent.setException(be);
			}
			_pbListener.buildFinished(endEvent);
			_pbListener.cleanup();
		}
	}
}
