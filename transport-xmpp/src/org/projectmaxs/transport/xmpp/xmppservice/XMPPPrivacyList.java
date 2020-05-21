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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverItems.Item;
import org.jivesoftware.smackx.privacy.PrivacyListManager;
import org.jivesoftware.smackx.privacy.packet.PrivacyItem;
import org.jivesoftware.smackx.privacy.packet.PrivacyItem.Type;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.Settings;

public class XMPPPrivacyList extends StateChangeListener {

	public static final String PRIVACY_LIST_NAME = GlobalConstants.NAME + "-v2";

	private static final Log LOG = Log.getLog();

	private static final List<PrivacyItem> PRIVACY_LIST = new LinkedList<PrivacyItem>();

	static {
		// Allow messages, iq and presence out, for JIDs that have a subscription to our presence
		// The idea is that we don't care about the presence of such IDs and therefore disallow, by
		// not filtering them, presence in stanzas, to reduce bandwidth
		// Hence first disallow incoming presence from entities which are subscribed to your
		// presence.
		// Note that XEP-0016 is not really clear if presence-in also means subscription requests
		// and thinks like 'subscribed', but it doesn't look that way: "<presence-in/> -- blocks
		// incoming presence notifications", which reads like only 'available' and 'unavailable' are
		// blocked.
		PrivacyItem subscribedToDeny = new PrivacyItem(Type.subscription,
				PrivacyItem.SUBSCRIPTION_TO, false, PRIVACY_LIST.size() + 1);
		subscribedToDeny.setFilterPresenceIn(true);
		PRIVACY_LIST.add(subscribedToDeny);
		// Then allow everything that is left.
		PrivacyItem subscribedToAllow = new PrivacyItem(Type.subscription,
				PrivacyItem.SUBSCRIPTION_TO, true, PRIVACY_LIST.size() + 1);
		PRIVACY_LIST.add(subscribedToAllow);

		// Stanzas from JIDs that have subscription state both are allowed. We use their presence in
		// stanza information to determine if there is a JID online that needs information in some
		// cases
		PrivacyItem subscribedBothAllow = new PrivacyItem(Type.subscription,
				PrivacyItem.SUBSCRIPTION_BOTH, true, PRIVACY_LIST.size() + 1);
		PRIVACY_LIST.add(subscribedBothAllow);

		// Fall-through case, because there is no type attribute, disallow
		PrivacyItem disallow = new PrivacyItem(false, Integer.MAX_VALUE);
		PRIVACY_LIST.add(disallow);
	}

	private final Settings mSettings;
	private PrivacyListManager mPrivacyListManager;

	XMPPPrivacyList(Settings settings) {
		mSettings = settings;
	}

	@Override
	public void newConnection(XMPPConnection connection) {
		mPrivacyListManager = PrivacyListManager.getInstanceFor(connection);
	}

	@Override
	public void connected(XMPPConnection connection) throws NotConnectedException {
		try {
			if (!mPrivacyListManager.isSupported()) return;
		} catch (Exception e) {
			LOG.e("isSupported", e);
			return;
		}

		if (!mSettings.privacyListsEnabled()) {
			try {
				mPrivacyListManager.declineDefaultList();
			} catch (Exception e) {
				LOG.e("Could not disable privacy lists", e);
			}
			return;
		}

		String defaultList = null;
		try {
			defaultList = mPrivacyListManager.getDefaultListName();
			LOG.d("Default privacy list: " + defaultList);
			// TODO We now assume if there is a privacy list with our name, then this list is
			// actually equal to the one we want. This should be changed so that the effective list
			// is in fact compared item-by-item with the desired list
			if (PRIVACY_LIST_NAME.equals(defaultList)) return;
		} catch (XMPPErrorException e) {
			// Log if not item-not-found(404)
			if (StanzaError.Condition.item_not_found.equals(e.getStanzaError().getCondition())) {
				LOG.e("connected", e);
			}
		} catch (InterruptedException | NoResponseException e) {
			LOG.e("connected", e);
		}
		try {
			setPrivacyList(connection);
		} catch (Exception e) {
			LOG.e("connected", e);
		}

	}

	private final void setPrivacyList(XMPPConnection connection) throws NoResponseException,
			XMPPErrorException, NotConnectedException, InterruptedException {
		List<PrivacyItem> list = new ArrayList<PrivacyItem>(PRIVACY_LIST.size() + 10);
		list.addAll(PRIVACY_LIST);

		// Whitelist all JIDs of the own service, e.g. conference.service.com, proxy.service.com
		for (Item i : ServiceDiscoveryManager.getInstanceFor(connection)
				.discoverItems(connection.getXMPPServiceDomain()).getItems()) {
			PrivacyItem allow = new PrivacyItem(Type.jid, i.getEntityID(), true, list.size() + 1);
			list.add(allow);
		}

		// This is an ugly workaround for XMPP servers that apply privacy lists also to stanzas
		// originating from themselves. For example http://issues.igniterealtime.org/browse/OF-724
		// Because there are such services in the wild and XEP-0016 is not clear on that topic, we
		// explicitly have to add a JID rule that allows stanzas from the service
		PrivacyItem allowService = new PrivacyItem(Type.jid, connection.getXMPPServiceDomain(), true,
				list.size() + 1);
		list.add(allowService);

		mPrivacyListManager.createPrivacyList(PRIVACY_LIST_NAME, list);
		mPrivacyListManager.setDefaultListName(PRIVACY_LIST_NAME);
	}
}
