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

import org.jivesoftware.smack.util.Async;
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
import org.projectmaxs.transport.xmpp.receivers.NetworkConnectivityReceiver;
import org.projectmaxs.transport.xmpp.util.Constants;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPEntityCapsCache;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPService;

import android.content.Intent;
import android.os.IBinder;

public class TransportService extends MAXSTransportService {

	public static final String TRANSPORT_OUTGOING_FILESERVICE = Constants.PACKAGE
			+ ".xmppservice.XMPPFileTransfer$MAXSOutgoingFileTransferService";

	public TransportService() {
		super("XMPP", TransportService.class);
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

		// We already unregister the receiver in onHandleIntent(), but in order to avoid leaking the
		// receiver, we make sure it's really unregistered by calling unregister() here, in
		// onDestroy(), again.
		NetworkConnectivityReceiver.unregister(this);

		final XMPPService xmppService = mXMPPService;
		if (xmppService != null) {
			// Ensure that all receivers are unregistered by calling XMPPService.disconnect(). We
			// need to perform that action async, since onDestory() is called from the main thread,
			// disconnect() is possible causing network IO and we want to avoid a
			// NetworkOnMainThreadException. Note that we can not use the Service's Looper, since it
			// will be already exited, because we already called super.onDestory().
			Async.go(new Runnable() {
				@Override
				public void run() {
					xmppService.disconnect();
				}
			});
		}

		XMPPEntityCapsCache.onDestroy(this);
		ServerPingWithAlarmManager.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// In order to avoid NetworkOnMainThread - some methods like
		// Socks5Proxy.getSocks5Proxy() do DNS lookups - exceptions, we
		// initialize the XMPP service here.
		if (mXMPPService == null) mXMPPService = XMPPService.getInstance(this);

		final String action = intent.getAction();
		LOG.d("onHandleIntent: " + action);
		switch (action) {
		case TransportConstants.ACTION_START_SERVICE:
			if (hasMessage(TransportConstants.ACTION_STOP_SERVICE.hashCode())) {
				LOG.d("Not starting service because there is a stop service action queued");
				break;
			}
			NetworkConnectivityReceiver.register(this);
			mXMPPService.connect();
			break;
		case TransportConstants.ACTION_STOP_SERVICE:
			if (hasMessage(TransportConstants.ACTION_START_SERVICE.hashCode())) {
				LOG.d("Not stopping service because there is a start service action queued");
				break;
			}
			NetworkConnectivityReceiver.unregister(this);
			mXMPPService.disconnect();
			stopSelf();
			break;
		case TransportConstants.ACTION_SET_STATUS:
			String status = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			mXMPPService.setStatus(status);
			break;
		case TransportConstants.ACTION_REQUEST_TRANSPORT_STATUS:
			mXMPPService.getHandleTransportStatus().sendStatus();
			break;
		case Constants.ACTION_SEND_AS_MESSAGE:
		case Constants.ACTION_SEND_AS_IQ:
			Message message = intent.getParcelableExtra(GlobalConstants.EXTRA_MESSAGE);
			CommandOrigin origin = intent
					.getParcelableExtra(TransportConstants.EXTRA_COMMAND_ORIGIN);
			mXMPPService.send(message, origin);
			break;
		case Constants.ACTION_NETWORK_CONNECTED:
			if (hasMessage(Constants.ACTION_NETWORK_CONNECTED.hashCode())) {
				LOG.d("Not handling NETWORK_CONNECTED because another intent of the same type is in the queue");
				break;
			}
			mXMPPService.connect();
			break;
		case Constants.ACTION_NETWORK_DISCONNECTED:
			if (hasMessage(Constants.ACTION_NETWORK_DISCONNECTED.hashCode())) {
				LOG.d("Not handling NETWORK_DISCONNECTED because another intent of the same type is in the queue");
				break;
			}
			mXMPPService.networkDisconnected();
			break;
		case Constants.ACTION_NETWORK_TYPE_CHANGED:
			if (hasMessage(Constants.ACTION_NETWORK_TYPE_CHANGED.hashCode())) {
				LOG.d("Not handling NETWORK_TYPE_CHANGED because another intent of the same type is in the queue");
				break;
			}
			if (mXMPPService.fastPingServer()) {
				LOG.d("Not issuing instantDisconnect as result of NETWORK_TYPE_CHANGED, because connection is (still/again) alive");
				break;
			}
			mXMPPService.instantDisconnect();
			break;
		default:
			throw new IllegalStateException("Unknown intent action: " + action);
		}
		LOG.d("onHandleIntent: " + action + " handled");
	}
}
