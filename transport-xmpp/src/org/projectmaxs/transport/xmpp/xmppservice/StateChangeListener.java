/*
    This file is part of Project MAXS.

    MAXS and its modules is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MAXS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MAXS.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.projectmaxs.transport.xmpp.xmppservice;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;

public class StateChangeListener {

	public void newConnection(XMPPConnection connection) {}

	public void connected(XMPPConnection connection) throws NotConnectedException {}

	/**
	 * Invoked when we got disconnected from an active connection
	 * 
	 * @param connection
	 *            the connection that got disconnected, may be null
	 */
	public void disconnected(XMPPConnection connection) {}

	/**
	 * Invoked when get return to disconnected state, but there was never an
	 * active connection. For example when something in the connection stage
	 * went wrong
	 * 
	 * @param reason
	 */
	public void disconnected(String reason) {}

	// These callback methods don't get access to the connection instance
	// because they will be called in the middle of a state change

	public void connecting() {

	}

	public void disconnecting() {

	}

	public void waitingForNetwork() {

	}

	public void waitingForRetry() {

	}

}
