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

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.util.Async;
import org.jivesoftware.smack.util.StringUtils;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.database.SendUnackedStanzasTable;

/**
 * Add send but unacknowledged stanzas to a database. The stanza entries in the database will get
 * removed once they are acknowledged by means of XEP-198: Stream Management. Only adds stanzas to
 * the database if stream management is enabled.
 */
public class SendStanzaDatabaseHandler extends StateChangeListener {

	private static Log LOG = Log.getLog();

	private final SendUnackedStanzasTable mSendUnackedStanzasTable;

	public SendStanzaDatabaseHandler(XMPPService xmppService) {
		mSendUnackedStanzasTable = SendUnackedStanzasTable.getInstance(xmppService.getContext());
	}

	@Override
	public void newConnection(final XMPPConnection newConnection) {
		if (!(newConnection instanceof XMPPTCPConnection)) {
			return;
		}
		final XMPPTCPConnection connection = (XMPPTCPConnection) newConnection;

		connection.addPacketSendingListener(new PacketListener() {
			@Override
			public void processPacket(Stanza stanza) throws NotConnectedException {
				// This only works if stream management is enabled
				if (!connection.isSmEnabled()) {
					return;
				}

				// If the stanza has no packet id, then we can't match it later
				if (StringUtils.isNullOrEmpty(stanza.getStanzaId())) {
					return;
				}

				// Filter out IQ requests
				if (stanza instanceof IQ) {
					IQ iq = (IQ) stanza;
					if (iq.isRequestIQ()) {
						return;
					}
				}
				mSendUnackedStanzasTable.addStanza(stanza);
			}
			// Match all stanza by using 'null' as filter
		}, null);

		// A listener that will remove stanzas from the database
		connection.addStanzaAcknowledgedListener(new PacketListener() {
			@Override
			public void processPacket(Stanza packet) throws NotConnectedException {
				String id = packet.getStanzaId();
				if (StringUtils.isNullOrEmpty(id)) {
					return;
				}
				mSendUnackedStanzasTable.removeId(id);
			}
		});
	}

	@Override
	public void connected(final XMPPConnection connection) {
		final List<Stanza> toResend = mSendUnackedStanzasTable.getAllAndDelete();
		if (toResend.isEmpty()) {
			return;
		}
		Async.go(new Runnable() {
			@Override
			public void run() {
				for (Stanza stanza : toResend) {
					try {
						connection.sendPacket(stanza);
					} catch (NotConnectedException e) {
						// Simply abort if sending the stanzas throws an exception. We could
						// consider re-adding the stanzas that weren't send to the database, but
						// right now, just abort.
						LOG.w("resend unacked stanzas got exception, aborting", e);
						break;
					}
				}
			}
		}, "Re-send unacked stanzas");
	}
}
