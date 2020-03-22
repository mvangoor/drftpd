package org.drftpd.slave.diskselection;

import org.drftpd.slave.Root;
import org.drftpd.slave.Slave;

import org.pf4j.ExtensionPoint;

public abstract class DiskSelection implements ExtensionPoint {

  private Slave _slave;

  public DiskSelection(Slave slave) {
    _slave = slave;
  }
	
	public Slave getSlaveObject() {
    return _slave;
  }
	
	public abstract Root getBestRoot(String dir);
}
