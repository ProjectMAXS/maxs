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

import org.jivesoftware.smack.tcp.BundleAndDefer;
import org.jivesoftware.smack.tcp.BundleAndDeferCallback;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.projectmaxs.shared.global.util.Log;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.OnNetworkActiveListener;
import android.os.Build;

/**
 * Bundle And Defer means that Smack will invoke the
 * {@link BundleAndDeferCallback#getBundleAndDeferMillis(BundleAndDefer)} callback once it is about
 * to send a stanza that could be deferred. If it is deferred, all following stanzas will get
 * bundled. The return value of the callback is the time the stanza, and all following, will get
 * deferred.
 * <p>
 * Together with he callback, Smack hands out an reference to a {@link BundleAndDefer} instance,
 * which allows us to abort the current deferring and send all bundled stanzas right away. We do
 * this once Android reports that the network become active.
 * </p>
 *
 */
public class XMPPBundleAndDefer {

	/**
	 * How long Smack defers outgoing stanzas if the current network is in high power (active)
	 * state.
	 */
	private static final int ACTIVE_STATE_DEFER_MILLIS = 150;

	/**
	 * How long Smack defers outgoing stanzas if the current network is not in high power (inactive)
	 * state.
	 */
	private static final int INACTIVE_STATE_DEFER_MILLIS = 23 * 1000;

	private static final Log LOG = Log.getLog();

	/**
	 * The current BundleAndDefer instance, which can be used to stop the current bundle and defer
	 * process by Smack. Once it's stopped, the bundled stanzas so far will be send immediately.
	 */
	private static BundleAndDefer currentBundleAndDefer;

	@TargetApi(21)
	public static void initialize(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			// This is all Android API 21. If we run on a lower API, then abort here.
			return;
		}

		final ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		BundleAndDeferCallback bundleAndDeferCallback = new BundleAndDeferCallback() {
			@Override
			public int getBundleAndDeferMillis(BundleAndDefer bundleAndDefer) {
				XMPPBundleAndDefer.currentBundleAndDefer = bundleAndDefer;
				final int deferMillis;
				final String networkState;
				if (connectivityManager.isDefaultNetworkActive()) {
					networkState = "active";
					deferMillis = ACTIVE_STATE_DEFER_MILLIS;
				} else {
					networkState = "incative";
					deferMillis = INACTIVE_STATE_DEFER_MILLIS;
				}

				if (LOG.isDebugLogEnabled()) {
					LOG.d("Returning " + deferMillis + " in getBundleAndDeferMillis(). Network is "
							+ networkState);
				}
				return deferMillis;
			}
		};
		XMPPTCPConnection.setDefaultBundleAndDeferCallback(bundleAndDeferCallback);

		connectivityManager.addDefaultNetworkActiveListener(new OnNetworkActiveListener() {
			@Override
			public void onNetworkActive() {
				final BundleAndDefer localCurrentbundleAndDefer = currentBundleAndDefer;
				if (localCurrentbundleAndDefer == null) {
					return;
				}
				LOG.d("onNetworkActive() invoked and currentbundleAndDefer not null, calling stopCurrentBundleAndDefer()");
				localCurrentbundleAndDefer.stopCurrentBundleAndDefer();
			}
		});
	}
}
