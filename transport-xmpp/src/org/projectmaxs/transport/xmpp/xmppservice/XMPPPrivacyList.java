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

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PrivacyList;
import org.jivesoftware.smack.PrivacyListManager;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.PrivacyItem;
import org.jivesoftware.smack.packet.PrivacyItem.PrivacyRule;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.Settings;

public class XMPPPrivacyList extends StateChangeListener {

	public static final String PRIVACY_LIST_NAME = GlobalConstants.NAME;

	private static final String JID = "jid";
	private static final String SUBSCRIPTION = "subscription";
	private static final String NAMESPACE = "jabber:iq:privacy";

	private static final Log LOG = Log.getLog();

	private static final List<PrivacyItem> PRIVACY_LIST = new LinkedList<PrivacyItem>();

	static {
		// Allow messages, iq and presence out, for JIDs that have a subscription to our presence
		// The idea is that we don't care about the presence of such IDs and therefore disallow, by
		// not filtering them, presence in stanzas, to reduce bandwidth
		PrivacyItem subscribedToAllow = new PrivacyItem(SUBSCRIPTION, true, 1);
		subscribedToAllow.setValue(PrivacyRule.SUBSCRIPTION_TO);
		subscribedToAllow.setFilterMessage(true);
		subscribedToAllow.setFilterIQ(true);
		subscribedToAllow.setFilterPresence_out(true);
		PRIVACY_LIST.add(subscribedToAllow);

		// Stanzas from JIDs that have subscription state both are allowed. We use their presence in
		// stanza information to determine if there is a JID online that needs information in some
		// cases
		PrivacyItem subscribedBothAllow = new PrivacyItem(SUBSCRIPTION, true, 2);
		subscribedBothAllow.setValue(PrivacyRule.SUBSCRIPTION_BOTH);
		PRIVACY_LIST.add(subscribedBothAllow);

		// Fall-through case, because there is no type attribute, disallow
		PrivacyItem disallow = new PrivacyItem(null, false, Integer.MAX_VALUE);
		PRIVACY_LIST.add(disallow);
	}

	private final Settings mSettings;
	private PrivacyListManager mPrivacyListManager;

	XMPPPrivacyList(Settings settings) {
		mSettings = settings;
	}

	@Override
	public void newConnection(Connection connection) {
		mPrivacyListManager = PrivacyListManager.getInstanceFor(connection);
	}

	@Override
	public void connected(Connection connection) {
		if (!isSupported(connection)) return;

		if (!mSettings.privacyListsEnabled()) {
			try {
				mPrivacyListManager.declineDefaultList();
			} catch (XMPPException e) {
				LOG.e("Could not disable privacy lists", e);
			}
			return;
		}

		PrivacyList defaultList = null;
		try {
			defaultList = mPrivacyListManager.getDefaultList();
			LOG.d("Default privacy list: " + defaultList);
			// TODO We now assume if there is a privacy list with our name, then this list is
			// actually equal to the one we want. This should be changed so that the activeList is
			// in fact compared item-by-item with the desired list
			if (defaultList.toString().equals(PRIVACY_LIST_NAME)) return;
		} catch (XMPPException e) {
			// Log if not item-not-found(404)
			if (e.getXMPPError().getCode() != 404) {
				LOG.e("connected", e);
			}
		}
		try {
			setPrivacyList(connection);
		} catch (XMPPException e) {
			LOG.e("connected", e);
		}

	}

	private final void setPrivacyList(Connection connection) throws XMPPException {
		// This is an ugly workaround for XMPP servers that apply privacy lists also to stanzas
		// originating from themselves. For example http://issues.igniterealtime.org/browse/OF-724
		List<PrivacyItem> list = new ArrayList<PrivacyItem>(PRIVACY_LIST.size() + 1);
		list.addAll(PRIVACY_LIST);
		// Because there are such services in the wild and XEP-0016 is not clear on that topic, we
		// explicitly have to add a JID rule that allows stanzas from the service
		PrivacyItem allowService = new PrivacyItem(JID, true, Integer.MAX_VALUE - 1);
		allowService.setValue(connection.getServiceName());
		list.add(allowService);

		mPrivacyListManager.createPrivacyList(PRIVACY_LIST_NAME, list);
		mPrivacyListManager.setDefaultListName(PRIVACY_LIST_NAME);
	}

	public static final boolean isSupported(Connection connection) {
		ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(connection);
		DiscoverInfo info;
		try {
			info = sdm.discoverInfo(connection.getServiceName());
		} catch (XMPPException e) {
			LOG.w("isSupported", e);
			return false;
		}

		return info.containsFeature(NAMESPACE);
	}
}
