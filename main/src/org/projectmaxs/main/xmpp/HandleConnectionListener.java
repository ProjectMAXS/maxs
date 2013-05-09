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
import org.projectmaxs.main.MAXSService;
import org.projectmaxs.main.StateChangeListener;

public class HandleConnectionListener extends StateChangeListener {

	private MAXSService.LocalService mMAXSLocalService;
	private ConnectionListener mConnectionListener;

	public HandleConnectionListener(MAXSService.LocalService maxsLocalService) {
		this.mMAXSLocalService = maxsLocalService;
	}

	@Override
	public void connected(Connection connection) {
		// TODO maybe only create and add new listener if newConnection
		mConnectionListener = new ConnectionListener() {

			@Override
			public void connectionClosed() {
				// TODO Auto-generated method stub

			}

			@Override
			public void connectionClosedOnError(Exception arg0) {
				// TODO Auto-generated method stub

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
