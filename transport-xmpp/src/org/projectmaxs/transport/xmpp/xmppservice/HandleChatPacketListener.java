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

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.Jid;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.smack.stanza.MAXSElement;

public class HandleChatPacketListener extends StateChangeListener {

	private static Log LOG = Log.getLog();

	private final StanzaListener mChatPacketListener;
	private final XMPPService mXMPPService;
	private final Settings mSettings;

	public HandleChatPacketListener(XMPPService xmppService) {
		mXMPPService = xmppService;
		mSettings = Settings.getInstance(xmppService.getContext());
		mChatPacketListener = new StanzaListener() {

			@Override
			public void processStanza(Stanza packet) {
				Message message = (Message) packet;
				Jid from = message.getFrom();

				if (MAXSElement.foundIn(packet)) {
					// Ignore messages with a MAXS element. This is done to prevent endless loops of
					// message sending between one or multiple MAXS instances.
					LOG.w("Ignoring message with MAXS element. jid='" + from + "' message='"
							+ message + '\'');
					return;
				}

				if (mSettings.isMasterJID(from)) {
					mXMPPService.newMessageFromMasterJID(message);
				} else {
					LOG.w("Ignoring message from non-master JID: jid='" + from + "' message='"
							+ message + '\'');
				}
			}

		};
	}

	@Override
	public void newConnection(XMPPConnection connection) {
		connection.addAsyncStanzaListener(mChatPacketListener, MessageTypeFilter.NORMAL_OR_CHAT);
	}

}
