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

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.sm.packet.StreamManagement;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.privacy.PrivacyListManager;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.DateTimeUtil;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.util.Constants;

import android.content.Context;
import android.content.Intent;

import java.util.Date;

public class HandleTransportStatus extends StateChangeListener {

	private static final Log LOG = Log.getLog();

	private final Context mContext;
	private final Settings mSettings;

	private String mStatusString;

	public HandleTransportStatus(Context context) {
		mContext = context;
		mStatusString = "inactive";
		mSettings = Settings.getInstance(context);
	}

	@Override
	public void connected(XMPPConnection connection) throws NotConnectedException {
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
		if (!mSettings.privacyListsEnabled()) {
			privacyListStatus = "privacy disabled";
		} else {
			final String privacyInactive = "privacy inactive";
			try {
				if (PrivacyListManager.getInstanceFor(connection).isSupported()) {
					String privacyListName = PrivacyListManager.getInstanceFor(connection)
							.getDefaultListName();
					if (XMPPPrivacyList.PRIVACY_LIST_NAME.equals(privacyListName)) {
						privacyListStatus = "privacy";
					} else {
						privacyListStatus = privacyInactive;
					}
				} else {
					privacyListStatus = "privacy not supported";
				}
			} catch (XMPPErrorException e) {
				if (StanzaError.Condition.item_not_found.equals(e.getStanzaError().getCondition())) {
					privacyListStatus = privacyInactive;
				} else {
					LOG.e("connected", e);
					privacyListStatus = "privacy unkown";
				}
			} catch (InterruptedException | NoResponseException e) {
				LOG.e("connected", e);
				privacyListStatus = "privacy unkown";
			}
		}

		String streamManagementStatus = "stream management ";
		if (connection instanceof XMPPTCPConnection) {
			if (mSettings.isStreamManagementEnabled()) {
				XMPPTCPConnection c = (XMPPTCPConnection) connection;
				if (c.hasFeature(StreamManagement.StreamManagementFeature.ELEMENT,
						StreamManagement.NAMESPACE)) {
					if (c.isSmEnabled()) {
						streamManagementStatus += "active";
					} else {
						streamManagementStatus += "not active";
					}
				} else {
					streamManagementStatus += "not supported";
				}
			} else {
				streamManagementStatus += "not enabled";
			}
		} else {
			streamManagementStatus += "not supported by connnection";

		}

		String authenticatedConnectionInitiallyEstablishedTimestampString = null;
		if (connection instanceof AbstractXMPPConnection) {
			AbstractXMPPConnection abstractXmppConnection = (AbstractXMPPConnection) connection;
			long authenticatedConnectionInitiallyEstablishedTimestamp = abstractXmppConnection.getAuthenticatedConnectionInitiallyEstablishedTimestamp();
			Date authenticatedConnectionInitiallyEstablishedTimestampDate = new Date(authenticatedConnectionInitiallyEstablishedTimestamp);
			authenticatedConnectionInitiallyEstablishedTimestampString = DateTimeUtil.toFullDate(authenticatedConnectionInitiallyEstablishedTimestampDate);
		}

		final String separator = ", ";
		StringBuilder sb = new StringBuilder(256);
		sb.append("connected (")
				.append(encryptionStatus)
				.append(separator)
				.append(compressionStatus)
				.append(separator)
				.append(privacyListStatus)
				.append(separator)
				.append(streamManagementStatus);
		if (authenticatedConnectionInitiallyEstablishedTimestampString != null) {
			sb.append(separator).append("since: ").append(authenticatedConnectionInitiallyEstablishedTimestampString);
		}
		sb.append(')');

		setAndSendStatus(sb.toString());
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
	public void waitingForRetry(String optionalReason) {
		if (!optionalReason.isEmpty()) {
			optionalReason = ": " + optionalReason;
		}
		setAndSendStatus("Waiting for connection retry" + optionalReason);
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
