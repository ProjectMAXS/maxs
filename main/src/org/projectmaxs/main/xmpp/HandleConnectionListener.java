package org.projectmaxs.main.xmpp;

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

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionListener;
import org.projectmaxs.main.Settings;
import org.projectmaxs.shared.util.Log;

public class HandleConnectionListener extends StateChangeListener {

	private static Log sLog = Log.getLog();

	private XMPPService mXMPPService;
	private ConnectionListener mConnectionListener;

	public HandleConnectionListener(XMPPService xmppService, Settings settings) {
		this.mXMPPService = xmppService;
		sLog.initialize(settings.getLogSettings());
	}

	@Override
	public void connected(Connection connection) {
		// TODO maybe only create and add new listener if newConnection
		mConnectionListener = new ConnectionListener() {

			@Override
			public void connectionClosed() {

				// TODO is this also called when the connection is closed by the
				// server? in that case -> reconnect, with something like
				// @formatter:off
				/*
				XMPPService.State current = mXMPPService.getCurrentState();
				switch (current) {
				case Connected:
					mXMPPService.reconnect();
					break;
				default:
					break;
				}
				*/
				// @formatter:on
			}

			@Override
			public void connectionClosedOnError(Exception arg0) {
				mXMPPService.reconnect();
			}

			@Override
			public void reconnectingIn(int arg0) {
				throw new IllegalStateException("Reconnection Manager is running");
			}

			@Override
			public void reconnectionFailed(Exception arg0) {
				throw new IllegalStateException("Reconnection Manager is running");
			}

			@Override
			public void reconnectionSuccessful() {
				throw new IllegalStateException("Reconnection Manager is running");
			}

		};
		connection.addConnectionListener(mConnectionListener);
	}

	@Override
	public void disconnected(Connection connection) {
		connection.removeConnectionListener(mConnectionListener);
		mConnectionListener = null;
	}

}
