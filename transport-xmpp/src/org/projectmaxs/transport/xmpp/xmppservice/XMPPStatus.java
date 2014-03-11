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

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPRoster.MasterJidListener;

import android.content.Context;

public class XMPPStatus extends StateChangeListener {

	private final XMPPRoster mXMPPRoster;
	private final Settings mSettings;

	private XMPPConnection mConnection;
	private String mActiveStatus = null;
	private String mDesiredStatus;

	protected XMPPStatus(XMPPRoster xmppRoster, Context context) {
		mXMPPRoster = xmppRoster;
		xmppRoster.addMasterJidListener(new MasterJidListener() {
			@Override
			public void masterJidAvailable() {
				sendStatus();
			}
		});
		mSettings = Settings.getInstance(context);
		// set the desired status to the last known status,
		mDesiredStatus = mSettings.getStatus();
	}

	protected void setStatus(String status) {
		mDesiredStatus = status;
		// prevent status form being send, when there is no active connection or
		// if the status message hasn't changed
		if (!mXMPPRoster.isMasterJidAvailable()
				|| (mActiveStatus != null && mActiveStatus.equals(mDesiredStatus))) return;
		sendStatus();
	}

	@Override
	public void newConnection(XMPPConnection connection) {
		mConnection = connection;
	}

	@Override
	public void connected(XMPPConnection connection) {
		sendStatus();
	}

	@Override
	public void disconnected(XMPPConnection connection) {}

	private void sendStatus() {
		if (mConnection == null || !mConnection.isAuthenticated()) return;
		Presence presence = new Presence(Presence.Type.available);
		presence.setStatus(mDesiredStatus);
		presence.setPriority(24);
		mConnection.sendPacket(presence);
		mActiveStatus = mDesiredStatus;
		mSettings.setStatus(mActiveStatus);
	}
}
