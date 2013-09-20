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

package org.projectmaxs.transport.xmpp.receivers;

import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.TransportService;
import org.projectmaxs.transport.xmpp.util.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkConnectivityReceiver extends BroadcastReceiver {

	private static final Log LOG = Log.getLog();

	@Override
	public void onReceive(Context context, Intent intent) {
		Settings settings = Settings.getInstance(context);

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (settings.isNetworkDebugLogEnabled()) {
			for (NetworkInfo networkInfo : cm.getAllNetworkInfo())
				log(networkInfo);
		}

		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			LOG.d("ActiveNetworkInfo follows:");
			log(activeNetworkInfo);
		}

		boolean connected;
		boolean networkTypeChanged;

		if (TransportService.isRunning()) {
			String lastActiveNetworkType = settings.getLastActiveNetwork();
			if (activeNetworkInfo != null) {
				// we have an active data connection
				String networkTypeName = activeNetworkInfo.getTypeName();
				connected = true;
				networkTypeChanged = false;
				if (!networkTypeName.equals(lastActiveNetworkType)) {
					LOG.d("networkTypeChanged current=" + networkTypeName + " last=" + lastActiveNetworkType);
					settings.setLastActiveNetwork(networkTypeName);
					networkTypeChanged = true;
				}
			} else {
				// we have *no* active data connection
				connected = false;
				if (lastActiveNetworkType.length() != 0) {
					networkTypeChanged = true;
				} else {
					networkTypeChanged = false;
				}
				settings.setLastActiveNetwork("");
			}
			LOG.d("Sending NETWORK_STATUS_CHANGED connected=" + connected + " changed=" + networkTypeChanged);
			Intent i = new Intent(Constants.ACTION_NETWORK_STATUS_CHANGED);
			i.putExtra(Constants.EXTRA_NETWORK_TYPE_CHANGED, networkTypeChanged);
			i.putExtra(Constants.EXTRA_NETWORK_CONNECTED, connected);
			context.startService(i);
		}
	}

	private static void log(NetworkInfo networkInfo) {
		// @formatter:off
		LOG.d("networkName=" + networkInfo.getTypeName()
				+ " available=" + networkInfo.isAvailable()
				+ ", connected=" + networkInfo.isConnected()
				+ ", connectedOrConnecting=" + networkInfo.isConnectedOrConnecting()
				+ ", failover=" + networkInfo.isFailover()
				+ ", roaming=" + networkInfo.isRoaming());
		// @formatter:on
	}
}
