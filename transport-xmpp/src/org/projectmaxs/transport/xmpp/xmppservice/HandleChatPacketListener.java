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

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.Settings;

public class HandleChatPacketListener extends StateChangeListener {

	private static Log LOG = Log.getLog();

	private final PacketListener mChatPacketListener;
	private final XMPPService mXMPPService;
	private final Settings mSettings;

	public HandleChatPacketListener(XMPPService xmppService) {
		mXMPPService = xmppService;
		mSettings = Settings.getInstance(xmppService.getContext());
		mChatPacketListener = new PacketListener() {

			@Override
			public void processPacket(Stanza packet) {
				Message message = (Message) packet;
				String fromString = message.getFrom();
				Jid from;
				try {
					from = JidCreate.from(fromString);
				} catch (XmppStringprepException e) {
					LOG.e("Not a valid 'from' JID string, ignoring message", e);
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
		connection.addAsyncPacketListener(mChatPacketListener, MessageTypeFilter.NORMAL_OR_CHAT);
	}

}
