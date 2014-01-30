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

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PrivacyListManager;
import org.jivesoftware.smack.XMPPException;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.transport.xmpp.util.Constants;

import android.content.Context;
import android.content.Intent;

public class HandleTransportStatus extends StateChangeListener {

	private static final Log LOG = Log.getLog();

	private final Context mContext;

	private String mStatusString;

	public HandleTransportStatus(Context context) {
		mContext = context;
		mStatusString = "inactive";
	}

	@Override
	public void connected(Connection connection) {
		String encryptionStatus;
		if (connection.isSecureConnection()) {
			encryptionStatus = "encrypted";
		} else {
			encryptionStatus = "unencrypted";
		}

		String compressionStatus;
		if (connection.isUsingCompression()) {
			compressionStatus = "compressed";
		} else {
			compressionStatus = "uncompressed";
		}

		String privacyListStatus;
		if (XMPPPrivacyList.isSupported(connection)) {
			final String privacyInactive = "privacy inactive";
			try {
				if (PrivacyListManager.getInstanceFor(connection).getActiveList().toString()
						.equals(XMPPPrivacyList.PRIVACY_LIST_NAME)) {
					privacyListStatus = "privacy";
				} else {
					privacyListStatus = privacyInactive;
				}
			} catch (XMPPException e) {
				if (e.getXMPPError().getCode() == 404) {
					privacyListStatus = privacyInactive;
				} else {
					LOG.e("connected", e);
					privacyListStatus = "privacy unkown";
				}
			}
		} else {
			privacyListStatus = "privacy not supported";
		}

		setAndSendStatus("connected (" + encryptionStatus + ", " + compressionStatus + ", "
				+ privacyListStatus + ")");
	}

	@Override
	public void disconnected(String reason) {
		if (reason.isEmpty()) {
			setAndSendStatus("disconnected");
		} else {
			setAndSendStatus("disconnected: " + reason);
		}
	}

	@Override
	public void connecting() {
		setAndSendStatus("connecting");
	}

	@Override
	public void disconnecting() {
		setAndSendStatus("disconnecting");
	}

	@Override
	public void waitingForNetwork() {
		setAndSendStatus("waiting for data connection");
	}

	@Override
	public void waitingForRetry() {
		setAndSendStatus("waiting for connection retry");
	}

	protected void setAndSendStatus(String status) {
		mStatusString = status;
		sendStatus();
	}

	public void sendStatus() {
		Intent intent = new Intent(TransportConstants.ACTION_UPDATE_TRANSPORT_STATUS);
		intent.setClassName(TransportConstants.MAIN_PACKAGE,
				TransportConstants.MAIN_TRANSPORT_SERVICE);
		intent.putExtra(GlobalConstants.EXTRA_PACKAGE, Constants.PACKAGE);
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, mStatusString);
		mContext.startService(intent);
	}
}