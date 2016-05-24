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

import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.transport.MAXSTransportService;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.TransportService;
import org.projectmaxs.transport.xmpp.util.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

public class NetworkConnectivityReceiver extends BroadcastReceiver {

	private static final Log LOG = Log.getLog();

	private static final BroadcastReceiver NETWORK_CONNECTIVITY_RECEIVER = new NetworkConnectivityReceiver();

	public static void register(Context context) {
		context.registerReceiver(NETWORK_CONNECTIVITY_RECEIVER, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
	}

	public static void unregister(Context context) {
		context.unregisterReceiver(NETWORK_CONNECTIVITY_RECEIVER);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		LOG.d("onReceive; intent=" + intent.getAction());
		Settings settings = Settings.getInstance(context);

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (settings.isNetworkDebugLogEnabled()) {
			logNetworks(cm);
		}

		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			LOG.d("ActiveNetworkInfo follows:");
			log(activeNetworkInfo);
		}

		if (!MAXSTransportService.isRunning()) {
			LOG.d("Service not running, aborting");
			return;
		}

		boolean connected;
		boolean networkTypeChanged;

		String lastActiveNetworkType = settings.getLastActiveNetwork();
		if (activeNetworkInfo != null) {
			// we have an active data connection
			String networkTypeName = activeNetworkInfo.getTypeName();
			connected = true;
			networkTypeChanged = false;
			if (!networkTypeName.equals(lastActiveNetworkType)) {
				LOG.d("networkTypeChanged current=" + networkTypeName + " last="
						+ lastActiveNetworkType);
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

		List<String> actions = new LinkedList<String>();
		// The order how we send those intents is important, NETWORK_TYPE_CHANGED must come first
		if (networkTypeChanged) {
			actions.add(Constants.ACTION_NETWORK_TYPE_CHANGED);
		}
		if (connected) {
			actions.add(Constants.ACTION_NETWORK_CONNECTED);
		} else {
			actions.add(Constants.ACTION_NETWORK_DISCONNECTED);
		}
		for (String action : actions) {
			Intent i = new Intent(context, TransportService.class);
			i.setAction(action);
			LOG.d("Sending action: " + action);
			context.startService(i);
		}
	}

	@SuppressWarnings("deprecation")
	private static void logNetworks(ConnectivityManager connectivityManager) {
		NetworkInfo[] networkInfos;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			networkInfos = connectivityManager.getAllNetworkInfo();
		} else {
			Network[] networks = connectivityManager.getAllNetworks();
			networkInfos = new NetworkInfo[networks.length];
			for (int i = 0; i < networks.length; i++) {
				networkInfos[i] = connectivityManager.getNetworkInfo(networks[i]);
			}
		}

		for (NetworkInfo networkInfo : networkInfos)
			log(networkInfo);
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
