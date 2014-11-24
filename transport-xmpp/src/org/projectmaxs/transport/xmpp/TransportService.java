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

import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.jul.JULHandler;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.CommandOrigin;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.maintransport.TransportInformation;
import org.projectmaxs.shared.maintransport.TransportInformation.TransportComponent;
import org.projectmaxs.shared.transport.MAXSTransportService;
import org.projectmaxs.transport.xmpp.util.Constants;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPEntityCapsCache;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPService;

import android.content.Intent;
import android.os.IBinder;

public class TransportService extends MAXSTransportService {

	public static final String TRANSPORT_OUTGOING_FILESERVICE = Constants.PACKAGE
			+ ".xmppservice.XMPPFileTransfer$MAXSOutgoingFileTransferService";

	public TransportService() {
		super("XMPP");
	}

	// @formatter:off
	public static final TransportInformation sTransportInformation = new TransportInformation(
			Constants.PACKAGE,
			"XMPP Transport",
			true,
			TRANSPORT_OUTGOING_FILESERVICE,
			new TransportComponent[] {
					new TransportComponent("Message", Constants.ACTION_SEND_AS_MESSAGE, true),
					new TransportComponent("IQ", Constants.ACTION_SEND_AS_IQ, false)
				}
			);
	// @formatter:on

	private static final Log LOG = Log.getLog();

	private XMPPService mXMPPService;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LOG.d("onCreate");
		JULHandler.init(Settings.getInstance(this));
		XMPPEntityCapsCache.onCreate(this);
		ServerPingWithAlarmManager.onCreate(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LOG.d("onDestroy");
		// Ensure that all receivers are unregistered by calling disconnect()
		if (mXMPPService != null) {
			mXMPPService.disconnect();
		}
		XMPPEntityCapsCache.onDestroy(this);
		ServerPingWithAlarmManager.onDestroy();
	}

	@Override
	public void onHandleIntent(Intent intent) {
		// In order to avoid NetworkOnMainThread - some methods like
		// Socks5Proxy.getSocks5Proxy() do DNS lookups - exceptions, we
		// initialize the XMPP service here.
		if (mXMPPService == null) mXMPPService = XMPPService.getInstance(this);

		final String action = intent.getAction();
		LOG.d("onHandleIntent: " + action);
		if (TransportConstants.ACTION_START_SERVICE.equals(action)) {
			if (hasMessage(TransportConstants.ACTION_STOP_SERVICE.hashCode())) {
				LOG.d("Not starting service because there is a stop service action queued");
			} else {
				mXMPPService.connect();
			}
		} else if (TransportConstants.ACTION_STOP_SERVICE.equals(action)) {
			if (hasMessage(TransportConstants.ACTION_START_SERVICE.hashCode())) {
				LOG.d("Not stopping service because there is a start service action queued");
			} else {
				mXMPPService.disconnect();
				stopSelf();
			}
		} else if (TransportConstants.ACTION_SET_STATUS.equals(action)) {
			String status = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			mXMPPService.setStatus(status);
		} else if (TransportConstants.ACTION_REQUEST_TRANSPORT_STATUS.equals(action)) {
			mXMPPService.getHandleTransportStatus().sendStatus();
		} else if (Constants.ACTION_SEND_AS_MESSAGE.equals(action)
				|| (Constants.ACTION_SEND_AS_IQ.equals(action))) {
			Message message = intent.getParcelableExtra(GlobalConstants.EXTRA_MESSAGE);
			CommandOrigin origin = intent
					.getParcelableExtra(TransportConstants.EXTRA_COMMAND_ORIGIN);
			mXMPPService.send(message, origin);
		} else if (Constants.ACTION_NETWORK_CONNECTED.equals(action)) {
			if (hasMessage(Constants.ACTION_NETWORK_CONNECTED.hashCode())) {
				LOG.d("Not handling NETWORK_CONNECTED because another intent of the same type is in the queue");
			} else {
				mXMPPService.connect();
			}
		} else if (Constants.ACTION_NETWORK_DISCONNECTED.equals(action)) {
			if (hasMessage(Constants.ACTION_NETWORK_DISCONNECTED.hashCode())) {
				LOG.d("Not handling NETWORK_DISCONNECTED because another intent of the same type is in the queue");
			} else {
				mXMPPService.networkDisconnected();
			}
		} else if (Constants.ACTION_NETWORK_TYPE_CHANGED.equals(action)) {
			if (hasMessage(Constants.ACTION_NETWORK_TYPE_CHANGED.hashCode())) {
				LOG.d("Not handling NETWORK_TYPE_CHANGED because another intent of the same type is in the queue");
			} else if (mXMPPService.fastPingServer()) {
				LOG.d("Not issuing instantDisconnect as result of NETWORK_TYPE_CHANGED, because connection already available");
			} else {
				mXMPPService.instantDisconnect();
			}
		} else {
			throw new IllegalStateException("Unknown intent action: " + action);
		}
		LOG.d("onHandleIntent: " + action + " handled");
	}
}
