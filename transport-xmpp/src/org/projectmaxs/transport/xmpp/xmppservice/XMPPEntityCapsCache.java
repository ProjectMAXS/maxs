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

import org.jivesoftware.smack.util.PacketParserUtils;
import org.jivesoftware.smackx.caps.EntityCapsManager;
import org.jivesoftware.smackx.caps.cache.EntityCapsPersistentCache;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.database.XMPPEntityCapsTable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class XMPPEntityCapsCache implements EntityCapsPersistentCache {

	private final static Log LOG = Log.getLog();

	static {
		EntityCapsManager.setDefaultEntityNode(GlobalConstants.HOMEPAGE_URL);
		// We assume the number of XMPP entities the MAXS account is able to retrieve the presence
		// from is not big and therefore we limit the Cache size to 50
		EntityCapsManager.setMaxsCacheSizes(50, 50);
	}

	private static XMPPEntityCapsCache sXMPPEntityCapsCache;

	public static void onCreate(Context context) {
		if (sXMPPEntityCapsCache != null) return;
		sXMPPEntityCapsCache = new XMPPEntityCapsCache(context);
	}

	public static void onDestroy(Context context) {
		if (sXMPPEntityCapsCache == null) return;
		context.unregisterReceiver(sXMPPEntityCapsCache.mStorageLowReceiver);
		sXMPPEntityCapsCache = null;
	}

	private final XMPPEntityCapsTable mXMPPEntityCapsTable;
	private final BroadcastReceiver mStorageLowReceiver;

	private XMPPEntityCapsCache(Context context) {
		mXMPPEntityCapsTable = XMPPEntityCapsTable.getInstance(context);
		EntityCapsManager.setPersistentCache(this);

		mStorageLowReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				LOG.i("ACTION_DEVICE_STORAGE_LOW received, emptying EntityCapsCache");
				emptyCache();
			}
		};
		context.registerReceiver(mStorageLowReceiver, new IntentFilter(
				Intent.ACTION_DEVICE_STORAGE_LOW));
	}

	@Override
	public void addDiscoverInfoByNodePersistent(String node, DiscoverInfo info) {
		mXMPPEntityCapsTable.addDiscoverInfo(node, info.toXML());
	}

	@Override
	public void emptyCache() {
		mXMPPEntityCapsTable.emptyTable();
	}

	@Override
	public DiscoverInfo lookup(String nodeVer) {
		String infoString = mXMPPEntityCapsTable.getDiscoverInfo(nodeVer);
		if (infoString == null) return null;

		try {
			return (DiscoverInfo) PacketParserUtils.parseStanza(infoString);
		} catch (Exception e) {
			LOG.e("Could not parse looked up DiscoverInfo from EntityCaps cache", e);
			return null;
		}
	}
}
