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

package org.projectmaxs.transport.xmpp;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.maintransport.TransportInformation;
import org.projectmaxs.shared.maintransport.TransportInformation.TransportComponent;
import org.projectmaxs.shared.transport.MAXSTransportService;
import org.projectmaxs.transport.xmpp.util.Constants;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPService;

import android.content.Intent;
import android.os.IBinder;

public class TransportService extends MAXSTransportService {

	public TransportService() {
		super("XMPP");
	}

	// @formatter:off
	public static final TransportInformation sTransportInformation = new TransportInformation(
			Constants.PACKAGE,
			"XMPP Transport",
			true,
			new TransportComponent[] {
					new TransportComponent("Message", Constants.ACTION_SEND_AS_MESSAGE, true),
					new TransportComponent("IQ", Constants.ACTION_SEND_AS_IQ, false)
				}
			);
	// @formatter:on

	private static final Log LOG = Log.getLog();
	private static boolean sIsRunning = false;

	public static boolean isRunning() {
		return sIsRunning;
	}

	private XMPPService mXMPPService;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		android.os.Debug.waitForDebugger();
		mXMPPService = XMPPService.getInstance(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			startService(new Intent(TransportConstants.ACTION_START_SERVICE));
			// Returning not sticky here, the start service intent will take
			// care of starting the service sticky
			return START_NOT_STICKY;
		}

		boolean stickyStart = true;
		final String action = intent.getAction();
		LOG.d("onStartCommand: action=" + action);
		if (TransportConstants.ACTION_START_SERVICE.equals(action)) {
			stickyStart = false;
			stopSelf(startId);
		}
		performInServiceHandler(intent);
		return stickyStart ? START_STICKY : START_NOT_STICKY;
	}

	@Override
	public void onHandleIntent(Intent intent) {
		final String action = intent.getAction();
		if (TransportConstants.ACTION_START_SERVICE.equals(action)) {
			sIsRunning = true;
			mXMPPService.connect();
		}
		else if (TransportConstants.ACTION_STOP_SERVICE.equals(action)) {
			mXMPPService.disconnect();
			sIsRunning = false;
		}
		else if (TransportConstants.ACTION_SET_STATUS.equals(action)) {
			String status = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			mXMPPService.setStatus(status);
		}
		else if (Constants.ACTION_SEND_AS_MESSAGE.equals(action) || (Constants.ACTION_SEND_AS_IQ.equals(action))) {
			Message message = intent.getParcelableExtra(GlobalConstants.EXTRA_MESSAGE);
			String originIssuerInfo = intent.getStringExtra(TransportConstants.EXTRA_ORIGIN_ISSUER_INFO);
			String originId = intent.getStringExtra(TransportConstants.EXTRA_ORIGIN_ID);
			mXMPPService.send(message, action, originIssuerInfo, originId);
		}
		else if (Constants.ACTION_NETWORK_STATUS_CHANGED.equals(action)) {
			String status = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			mXMPPService.setStatus(status);
		}
		else {
			throw new IllegalStateException("Unkown intent action: " + action);
		}
	}
}
