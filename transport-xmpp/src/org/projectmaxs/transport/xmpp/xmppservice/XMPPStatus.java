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

import java.util.List;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.projectmaxs.shared.global.StatusInformation;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.CurrentStatus;
import org.projectmaxs.shared.transport.MAXSTransportService;
import org.projectmaxs.transport.xmpp.util.Constants;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPRoster.MasterJidListener;

import android.content.Context;

public class XMPPStatus extends StateChangeListener {
	private static final Log LOG = Log.getLog();

	private final XMPPRoster mXMPPRoster;

	private XMPPConnection mConnection;
	private CurrentStatus mActiveStatus = null;
	private CurrentStatus mDesiredStatus;

	protected XMPPStatus(XMPPRoster xmppRoster, Context context) {
		mXMPPRoster = xmppRoster;
		xmppRoster.addMasterJidListener(new MasterJidListener() {
			@Override
			public void masterJidAvailable() {
				sendStatus();
			}
		});

		// Request the current status from MAXS main.
		MAXSTransportService.requestMaxsStatusUpdate(context, Constants.PACKAGE);
	}

	protected void setStatus(CurrentStatus status) {
		mDesiredStatus = status;
		// prevent status form being send, when there is no active connection or
		// if the status message hasn't changed
		if (!mXMPPRoster.isMasterJidAvailable() || (mActiveStatus != null
				&& mActiveStatus.getStatusString().equals(mDesiredStatus.getStatusString()))) {
			return;
		}
		sendStatus();
	}

	@Override
	public void newConnection(XMPPConnection connection) {
		mConnection = connection;
	}

	@Override
	public void connected(XMPPConnection connection) {
		sendStatus();
	}

	@Override
	public void disconnected(XMPPConnection connection) {}

	private void sendStatus() {
		if (mConnection == null || !mConnection.isAuthenticated()) return;

		final CurrentStatus currentStatus = mDesiredStatus;
		if (currentStatus == null) {
			return;
		}

		Presence presence = new Presence(Presence.Type.available);
		presence.setStatus(currentStatus.getStatusString());
		presence.addExtension(
				new MaxsStatusExtensionElement(currentStatus.getStatusInformationList()));

		try {
			mConnection.sendStanza(presence);
		} catch (InterruptedException | NotConnectedException e) {
			LOG.w("Could not set own presence", e);
			return;
		}

		mActiveStatus = mDesiredStatus;
	}

	private class MaxsStatusExtensionElement implements ExtensionElement {

		public static final String ELEMENT = "maxs-status";
		public static final String NAMESPACE = "https://projectmaxs.org";

		private final List<StatusInformation> statusInformationList;

		private MaxsStatusExtensionElement(List<StatusInformation> statusInformationList) {
			this.statusInformationList = statusInformationList;
		}

		@Override
		public String getElementName() {
			return ELEMENT;
		}

		@Override
		public String getNamespace() {
			return NAMESPACE;
		}

		@Override
		public XmlStringBuilder toXML(XmlEnvironment xmlEnvironment) {
			XmlStringBuilder xml = new XmlStringBuilder(this);
			xml.rightAngleBracket();
			for (StatusInformation statusInformation : statusInformationList) {
				xml.element(statusInformation.getKey(), statusInformation.getMachineValue());
			}
			xml.closeElement(this);
			return xml;
		}

	}
}
