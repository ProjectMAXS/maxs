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

public class XMPPPrivacyList extends StateChangeListener {

	public static final String PRIVACY_LIST_NAME = GlobalConstants.NAME;

	private static final String SUBSCRIPTION = "subscription";
	private static final String NAMESPACE = "jabber:iq:privacy";

	private static final Log LOG = Log.getLog();

	private static final List<PrivacyItem> PRIVACY_LIST = new LinkedList<PrivacyItem>();

	static {
		PrivacyItem subsToMessagesAllow = new PrivacyItem(SUBSCRIPTION, true, 1);
		subsToMessagesAllow.setValue(PrivacyRule.SUBSCRIPTION_TO);
		subsToMessagesAllow.setFilterMessage(true);
		PRIVACY_LIST.add(subsToMessagesAllow);

		PrivacyItem subsBothAllAllow = new PrivacyItem(SUBSCRIPTION, true, 2);
		subsToMessagesAllow.setValue(PrivacyRule.SUBSCRIPTION_BOTH);
		PRIVACY_LIST.add(subsBothAllAllow);

		PrivacyItem disallow = new PrivacyItem(null, false, 3);
		PRIVACY_LIST.add(disallow);
	}

	PrivacyListManager mPrivacyListManager;

	@Override
	public void newConnection(Connection connection) {
		mPrivacyListManager = PrivacyListManager.getInstanceFor(connection);
	}

	@Override
	public void connected(Connection connection) {
		if (!isSupported(connection)) {
			LOG.i("PrivacyLists not supported by server");
			return;
		}

		PrivacyList activeList = null;
		try {
			activeList = mPrivacyListManager.getActiveList();
			// TODO We now assume if there is a privacy list with our name, then this list is
			// actually equal to the one we want. This should be changed so that the activeList is
			// in fact compared item-by-item with the desired list
			if (activeList.toString().equals(PRIVACY_LIST_NAME)) return;
		} catch (XMPPException e) {
			// Log if not item-not-found(404)
			if (e.getXMPPError().getCode() != 404) {
				LOG.e("connected", e);
			}
		}
		try {
			setPrivacyList();
		} catch (XMPPException e) {
			LOG.e("connected", e);
		}

	}

	private final void setPrivacyList() throws XMPPException {
		mPrivacyListManager.createPrivacyList(PRIVACY_LIST_NAME, PRIVACY_LIST);
		mPrivacyListManager.setDefaultListName(PRIVACY_LIST_NAME);
		mPrivacyListManager.setActiveListName(PRIVACY_LIST_NAME);
	}

	private static final boolean isSupported(Connection connection) {
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
