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
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.projectmaxs.transport.xmpp.Settings;

public class HandleChatPacketListener extends StateChangeListener {

	private final PacketListener mChatPacketListener;
	private final XMPPService mXMPPService;
	private final Settings mSettings;

	public HandleChatPacketListener(XMPPService xmppService) {
		mXMPPService = xmppService;
		mSettings = Settings.getInstance(xmppService.getContext());
		mChatPacketListener = new PacketListener() {

			@Override
			public void processPacket(Packet packet) {
				Message message = (Message) packet;
				String from = message.getFrom();
				if (mSettings.isMasterJID(from)) mXMPPService.newMessageFromMasterJID(message);
			}

		};
	}

	@Override
	public void connected(Connection connection) {
		connection.addPacketListener(mChatPacketListener, new MessageTypeFilter(Message.Type.chat));
	}

	@Override
	public void disconnected(Connection connection) {
		connection.removePacketListener(mChatPacketListener);
	}

}
