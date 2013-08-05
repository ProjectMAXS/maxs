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

package org.projectmaxs.main.xmpp;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.packet.Presence;
import org.projectmaxs.main.StateChangeListener;
import org.projectmaxs.main.xmpp.XMPPRoster.MasterJidListener;

public class XMPPStatus extends StateChangeListener {

	private final XMPPRoster mXMPPRoster;

	private Connection mConnection;
	private String mActiveStatus = "";
	private String mDesiredStatus = "";

	protected XMPPStatus(XMPPRoster xmppRoster) {
		mXMPPRoster = xmppRoster;
		xmppRoster.addMasterJidListener(new MasterJidListener() {
			@Override
			public void masterJidAvailable() {
				super.masterJidAvailable();
			}
		});
	}

	protected void setStatus(String status) {
		mDesiredStatus = status;

		if (mConnection == null || !mConnection.isAuthenticated() || !mXMPPRoster.isMasterJidAvailable()) return;

		Presence presence = new Presence(Presence.Type.available);
		presence.setStatus(status);
		presence.setPriority(24);
		mConnection.sendPacket(presence);

		mActiveStatus = status;
	}

	@Override
	public void newConnection(Connection connection) {
		mConnection = connection;
	}

	// @Override
	// public void connected(Connection connection) {
	// masterJidAvailable();
	// }

	@Override
	public void disconnected(Connection connection) {
	}

	private void masterJidAvailable() {
		if (mDesiredStatus.equals(mActiveStatus)) return;
		setStatus(mDesiredStatus);
	}
}
