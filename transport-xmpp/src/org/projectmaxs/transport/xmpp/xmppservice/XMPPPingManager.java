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

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.projectmaxs.shared.global.util.Log;

public class XMPPPingManager extends StateChangeListener implements PingFailedListener {

	public static final int PING_INTERVAL_SECONDS = 60 * 30; // 30 minutes

	private static final Log LOG = Log.getLog();

	private final XMPPService mXMPPService;

	protected XMPPPingManager(XMPPService service) {
		mXMPPService = service;
	}

	@Override
	public void newConnection(Connection connection) {
		// setPingIntervall takes seconds (!) as parameter
		PingManager.getInstanceFor(connection).setPingIntervall(PING_INTERVAL_SECONDS);
	}

	@Override
	public void connected(Connection connection) {
		PingManager.getInstanceFor(connection).registerPingFailedListener(this);
	}

	@Override
	public void disconnected(Connection connection) {
		PingManager.getInstanceFor(connection).unregisterPingFailedListener(this);
	}

	@Override
	public void pingFailed() {
		LOG.w("ping failed: issuing reconnect");
		mXMPPService.reconnect();
	}

}
