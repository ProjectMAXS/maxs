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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.roster.RosterUtil;
import org.jivesoftware.smack.roster.SubscribeListener;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.Jid;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.Settings;

public class XMPPRoster extends StateChangeListener implements RosterListener {
	private static final Log LOG = Log.getLog();

	private final List<MasterJidListener> mMasterJidListeners = new LinkedList<MasterJidListener>();

	private Settings mSettings;
	private Roster mRoster;
	private XMPPConnection mConnection;
	private boolean mMasterJidAvailable;

	public XMPPRoster(Settings settings) {
		mSettings = settings;
	}

	/*
	 * StateChangeListener callbacks
	 */

	@Override
	public void newConnection(XMPPConnection connection) {
		mConnection = connection;
		mRoster = Roster.getInstanceFor(connection);
		mRoster.addRosterListener(this);

		mRoster.addSubscribeListener(new SubscribeListener() {
			@Override
			public SubscribeAnswer processSubscribe(Jid from, Presence subscribeRequest) {
				Set<EntityBareJid> masterJids = mSettings.getMasterJids();
				for (EntityBareJid masterJid : masterJids) {
					if (masterJid.equals(from)) {
						return SubscribeAnswer.Approve;
					}
				}
				return SubscribeAnswer.Deny;
			}
		});
	}

	@Override
	public void connected(XMPPConnection connection) {
		Set<EntityBareJid> masterJids = mSettings.getMasterJids();
		for (EntityBareJid jid : masterJids)
			friendJid(jid);

		checkMasterJids();
	}

	@Override
	public void disconnected(XMPPConnection connection) {
		mMasterJidAvailable = false;
	}

	/*
	 * RosterListener callbacks
	 */

	@Override
	public void entriesAdded(Collection<Jid> arg0) {}

	@Override
	public void entriesDeleted(Collection<Jid> arg0) {}

	@Override
	public void entriesUpdated(Collection<Jid> arg0) {}

	@Override
	public void presenceChanged(Presence presence) {
		checkMasterJids();
	}

	protected boolean isMasterJidAvailable() {
		return mMasterJidAvailable;
	}

	protected void addMasterJidListener(MasterJidListener listener) {
		mMasterJidListeners.add(listener);
	}

	protected void removeMasterJidListener(MasterJidListener listener) {
		mMasterJidListeners.remove(listener);
	}

	private void checkMasterJids() {
		boolean masterJidAvailable = false;
		for (BareJid jid : mSettings.getMasterJids()) {
			Presence presence = mRoster.getPresence(jid);
			if (presence.isAvailable()) {
				if (mSettings.isExcludedResource(presence.getFrom().getResourceOrNull())) {
					// Skip excluded resources
					continue;
				}
				masterJidAvailable = true;
				// we found at least one available master JID, break here
				break;
			}
		}

		if (mMasterJidAvailable == false && masterJidAvailable == true) {
			for (MasterJidListener listener : mMasterJidListeners)
				listener.masterJidAvailable();
		}

		mMasterJidAvailable = masterJidAvailable;
	}

	/**
	 * Subscribe and request subscription with a given JID. Essentially become a
	 * "friend" of the JID.
	 * 
	 * @param userID
	 */
	private void friendJid(BareJid userID) {
		if (!mRoster.contains(userID)) {
			try {
				mRoster.createItemAndRequestSubscription(userID, userID.toString(), null);
				grantSubscription(userID, mConnection);
			} catch (NotConnectedException | InterruptedException | NotLoggedInException
					| NoResponseException | XMPPErrorException e) {
				LOG.w("Exception creating roster entry for " + userID, e);
				return;
			}
		}
		RosterEntry rosterEntry = mRoster.getEntry(userID);
		// async code here, the server may not have added the entry yet, so bail
		// out here
		if (rosterEntry == null) return;

		RosterPacket.ItemType type = rosterEntry.getType();
		try {
			switch (type) {
			case from:
				RosterUtil.askForSubscriptionIfRequired(mRoster, userID);
				break;
			case to:
				grantSubscription(userID, mConnection);
				break;
			case none:
				grantSubscription(userID, mConnection);
				RosterUtil.askForSubscriptionIfRequired(mRoster, userID);
				break;
			case both:
			default:
				break;
			}
		} catch (NotLoggedInException | NotConnectedException | InterruptedException e) {
			LOG.w("Exception handling subscription for " + userID, e);
		}
	}

	/**
	 * grants the given JID the subscription (e.g. viewing your online state)
	 * 
	 * @param jid
	 * @param connection
	 * @throws InterruptedException
	 * @throws NotConnectedException
	 */
	private static void grantSubscription(BareJid jid, XMPPConnection connection)
			throws NotConnectedException, InterruptedException {
		Presence presence = connection.getStanzaFactory().buildPresenceStanza()
				.ofType(Presence.Type.subscribed)
				.to(jid)
				.build();
		connection.sendStanza(presence);
	}

	public static class MasterJidListener {
		public void masterJidAvailable() {}
	}
}
