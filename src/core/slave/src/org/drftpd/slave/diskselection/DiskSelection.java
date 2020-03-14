package org.drftpd.slave.diskselection;

import org.drftpd.slave.Root;
import org.drftpd.slave.Slave;

import org.pf4j.ExtensionPoint;

public interface DiskSelection extends ExtensionPoint {
	
	public Slave getSlaveObject();
	
	public abstract Root getBestRoot(String dir);
}
