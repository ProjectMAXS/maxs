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
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.transport.xmpp.util.Constants;

import android.content.Context;
import android.content.Intent;

public class HandleTransportStatus extends StateChangeListener {

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
		setAndSendStatus("connected (" + encryptionStatus + ", " + compressionStatus + ")");
	}

	@Override
	public void disconnected() {
		setAndSendStatus("disconnected");
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
		intent.putExtra(GlobalConstants.EXTRA_PACKAGE, Constants.PACKAGE);
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, mStatusString);
		mContext.startService(intent);
	}
}