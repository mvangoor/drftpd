/*
 * This file is part of DrFTPD, Distributed FTP Daemon.
 *
 * DrFTPD is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * DrFTPD is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * DrFTPD; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */
package org.drftpd.protocol.slave;

import java.lang.reflect.Method;

/**
 * Simple helper class to keep code cleaner.
 * @author fr0w
 * @version $Id$
 */
public class HandlerWrapper {
	private SlaveHandler _sh;
	private Method _method;
	
	public HandlerWrapper(SlaveHandler sh, Method method) {
		_sh = sh;
		_method = method;
	}
	
	public Method getMethod() {
		return _method;
	}
	
	public SlaveHandler getAsyncHandler() {
		return _sh;
	}
}
