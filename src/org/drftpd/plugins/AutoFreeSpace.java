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
package org.drftpd.plugins;

import net.sf.drftpd.event.Event;
import net.sf.drftpd.event.FtpListener;
import net.sf.drftpd.event.TransferEvent;
import net.sf.drftpd.master.config.ExcludePath;
import net.sf.drftpd.master.config.FtpConfig;

import org.apache.log4j.Logger;

import org.apache.oro.text.regex.MalformedPatternException;
import org.drftpd.Bytes;
import org.drftpd.PropertyHelper;
import org.drftpd.master.ConnectionManager;
import org.drftpd.remotefile.LinkedRemoteFile;
import org.drftpd.remotefile.LinkedRemoteFileInterface;
import org.drftpd.remotefile.RemoteFileLastModifiedComparator;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;


/**
 * @author zubov
 * @version $Id: AutoFreeSpace.java 806 2004-11-17 22:29:13Z mog $
 */
public class AutoFreeSpace implements FtpListener {
    private static final Logger logger = Logger.getLogger(AutoFreeSpace.class);
    private long _archiveAfter;
    private ConnectionManager _cm;
    private long _cycleTime;
    private ArrayList _exemptList;
    private long _keepFree;
    private long _lastchecked = System.currentTimeMillis();

    public AutoFreeSpace() {
        reload();
    }

    public void actionPerformed(Event event) {
        if (event.getCommand().equals("RELOAD")) {
            reload();
        }

        if (!(event instanceof TransferEvent) ||
                !event.getCommand().equals("STOR")) {
            return;
        }

        TransferEvent transevent = (TransferEvent) event;

        if (!transevent.isComplete()) {
            return;
        }

        if ((System.currentTimeMillis() - _lastchecked) <= _cycleTime) {
            return;
        }

        _lastchecked = System.currentTimeMillis();

        //		Collection slaveList = null;
        //		try {
        //			slaveList = _cm.getSlaveManager().getAvailableSlaves();
        //		} catch (NoAvailableSlaveException e) {
        //			// done, can't remove space from nonexistant slaves
        //		}
        //		while (true) {
        //			RemoteSlave rslave = _cm.getSlaveManager().findSmallestFreeSlave();
        //			SlaveStatus status = null;
        //			try {
        //				status = rslave.getStatus();
        //				if (_keepFree > status.getDiskSpaceAvailable()) {
        //					deleteOldReleases(rslave);
        //				} else {
        //					break;
        //				}
        //			} catch (RemoteException e1) {
        //				rslave.handleRemoteException(e1);
        //			} catch (NoAvailableSlaveException e1) {
        //			}
        //		}
    }

    /**
     * @param lrf
     * Returns true if lrf.getPath() is excluded
     */
    public boolean checkExclude(LinkedRemoteFileInterface lrf) {
        for (Iterator iter = _exemptList.iterator(); iter.hasNext();) {
            ExcludePath ep = (ExcludePath) iter.next();

            if (ep.checkPath(lrf)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Deletes the oldest directories until _keepFree space is available.
     * @param dirs
     */
    private void deleteOldReleases(Collection rootDirs, long spaceAvailable) {
        //Collection rootDirs = getConnectionManager().getSlaveManager().getRoot().getDirectories();
        ArrayList dirs = new ArrayList();

        for (Iterator iter = rootDirs.iterator(); iter.hasNext();) {
            LinkedRemoteFile lrf = (LinkedRemoteFile) iter.next();

            if (checkExclude(lrf)) {
                logger.debug(lrf.getPath() + " is excluded");

                continue;
            }

            dirs.addAll(lrf.getDirectories());
        }

        Collections.sort(dirs, new RemoteFileLastModifiedComparator(true));

        //		long spaceAvailable =
        //			getConnectionManager()
        //				.getSlaveManager()
        //				.getAllStatus()
        //				.getDiskSpaceAvailable();
        if (spaceAvailable < _keepFree) {
            for (Iterator iter = dirs.iterator(); iter.hasNext();) {
                logger.debug(Bytes.formatBytes(spaceAvailable) + " < " +
                    Bytes.formatBytes(_keepFree));

                if (spaceAvailable >= _keepFree) {
                    break;
                }

                LinkedRemoteFileInterface file = (LinkedRemoteFileInterface) iter.next();
                spaceAvailable += file.length();
                logger.info("delete " + file.getPath());
                file.delete();
            }
        }
    }

    public void init(ConnectionManager connectionManager) {
        _cm = connectionManager;
    }

    private void reload() {
        Properties props = new Properties();

        try {
            props.load(new FileInputStream("conf/autofreespace.conf"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        reload(props);
    }

    private void reload(Properties props) {
        _cycleTime = 60000 * Long.parseLong(PropertyHelper.getProperty(props,
                    "cycleTime"));
        _keepFree = Bytes.parseBytes(PropertyHelper.getProperty(props, "keepFree"));
        _archiveAfter = 60000 * Long.parseLong(PropertyHelper.getProperty(props,
                    "archiveAfter"));
        _lastchecked = System.currentTimeMillis();
        _exemptList = new ArrayList();

        for (int i = 1;; i++) {
            String path = props.getProperty("exclude." + i);

            if (path == null) {
                break;
            }

            try {
                ExcludePath.makePermission(_exemptList, path);
            } catch (MalformedPatternException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    public void unload() {
    }
}