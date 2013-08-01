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

import java.util.Collection;
import java.util.Set;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smack.util.StringUtils;
import org.projectmaxs.main.Settings;
import org.projectmaxs.main.StateChangeListener;

public class XMPPRoster extends StateChangeListener implements RosterListener {

	private Settings mSettings;
	private Roster mRoster;
	private Connection mConnection;

	public XMPPRoster(Settings settings) {
		mSettings = settings;
	}

	/*
	 * StateChangeListener callbacks
	 */

	@Override
	public void newConnection(Connection connection) {
		mConnection = connection;
		mRoster = connection.getRoster();
	};

	@Override
	public void connected(Connection connection) {
		Set<String> masterJids = mSettings.getMasterJids();
		for (String jid : masterJids)
			friendJid(jid);
	}

	/*
	 * RosterListener callbacks
	 */

	@Override
	public void entriesAdded(Collection<String> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void entriesDeleted(Collection<String> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void entriesUpdated(Collection<String> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void presenceChanged(Presence arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * Subscribe and request subscription with a given JID. Essentially become a
	 * "friend" of the JID.
	 * 
	 * @param userID
	 */
	private void friendJid(String userID) {
		if (!mRoster.contains(userID)) {
			try {
				mRoster.createEntry(userID, StringUtils.parseBareAddress(userID), null);
				grantSubscription(userID, mConnection);
				requestSubscription(userID, mConnection);
			} catch (XMPPException e) {
				// TODO
				return;
			}
		}
		RosterEntry rosterEntry = mRoster.getEntry(userID);
		// async code here, the server may not have added the entry yet, so bail
		// out here
		if (rosterEntry == null) return;

		RosterPacket.ItemType type = rosterEntry.getType();
		switch (type) {
		case from:
			requestSubscription(userID, mConnection);
			break;
		case to:
			grantSubscription(userID, mConnection);
			break;
		case none:
			grantSubscription(userID, mConnection);
			requestSubscription(userID, mConnection);
			break;
		case both:
		default:
			break;
		}
	}

	/**
	 * grants the given JID the subscription (e.g. viewing your online state)
	 * 
	 * @param jid
	 * @param connection
	 */
	private static void grantSubscription(String jid, Connection connection) {
		Presence presence = new Presence(Presence.Type.subscribed);
		sendPresenceTo(jid, presence, connection);
	}

	/**
	 * request the subscription from a given JID
	 * 
	 * @param jid
	 * @param connection
	 */
	private static void requestSubscription(String jid, Connection connection) {
		Presence presence = new Presence(Presence.Type.subscribe);
		sendPresenceTo(jid, presence, connection);
	}

	private static void sendPresenceTo(String to, Presence presence, Connection connection) {
		presence.setTo(to);
		connection.sendPacket(presence);
	}
}
