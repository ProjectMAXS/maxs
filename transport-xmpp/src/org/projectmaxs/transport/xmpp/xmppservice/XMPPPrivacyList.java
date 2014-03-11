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

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.privacy.PrivacyList;
import org.jivesoftware.smackx.privacy.PrivacyListManager;
import org.jivesoftware.smackx.privacy.packet.PrivacyItem;
import org.jivesoftware.smackx.privacy.packet.PrivacyItem.Type;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.Settings;

public class XMPPPrivacyList extends StateChangeListener {

	public static final String PRIVACY_LIST_NAME = GlobalConstants.NAME;

	private static final Log LOG = Log.getLog();

	private static final List<PrivacyItem> PRIVACY_LIST = new LinkedList<PrivacyItem>();

	static {
		// Allow messages, iq and presence out, for JIDs that have a subscription to our presence
		// The idea is that we don't care about the presence of such IDs and therefore disallow, by
		// not filtering them, presence in stanzas, to reduce bandwidth
		PrivacyItem subscribedToAllow = new PrivacyItem(Type.subscription,
				PrivacyItem.SUBSCRIPTION_TO, true, 1);
		subscribedToAllow.setFilterMessage(true);
		subscribedToAllow.setFilterIQ(true);
		subscribedToAllow.setFilterPresenceOut(true);
		PRIVACY_LIST.add(subscribedToAllow);

		// Stanzas from JIDs that have subscription state both are allowed. We use their presence in
		// stanza information to determine if there is a JID online that needs information in some
		// cases
		PrivacyItem subscribedBothAllow = new PrivacyItem(Type.subscription,
				PrivacyItem.SUBSCRIPTION_BOTH, true, 2);
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
	public void connected(XMPPConnection connection) {
		try {
			if (!mPrivacyListManager.isSupported()) return;
		} catch (XMPPException e) {
			LOG.e("isSupported", e);
			return;
		}

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
			if (defaultList.getName().equals(PRIVACY_LIST_NAME)) return;
		} catch (XMPPException e) {
			// Log if not item-not-found(404)
			if (XMPPError.Condition.item_not_found.equals(e.getXMPPError().getCondition())) {
				LOG.e("connected", e);
			}
		}
		try {
			setPrivacyList(connection);
		} catch (XMPPException e) {
			LOG.e("connected", e);
		}

	}

	private final void setPrivacyList(XMPPConnection connection) throws XMPPException {
		// This is an ugly workaround for XMPP servers that apply privacy lists also to stanzas
		// originating from themselves. For example http://issues.igniterealtime.org/browse/OF-724
		List<PrivacyItem> list = new ArrayList<PrivacyItem>(PRIVACY_LIST.size() + 1);
		list.addAll(PRIVACY_LIST);
		// Because there are such services in the wild and XEP-0016 is not clear on that topic, we
		// explicitly have to add a JID rule that allows stanzas from the service
		PrivacyItem allowService = new PrivacyItem(Type.jid, connection.getServiceName(), true,
				Integer.MAX_VALUE - 1);
		list.add(allowService);

		mPrivacyListManager.createPrivacyList(PRIVACY_LIST_NAME, list);
		mPrivacyListManager.setDefaultListName(PRIVACY_LIST_NAME);
	}
}
