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

import org.jivesoftware.smack.Connection;
import org.projectmaxs.transport.xmpp.database.MessagesTable;
import org.projectmaxs.transport.xmpp.database.MessagesTable.Entry;

public class HandleMessagesListener extends StateChangeListener {
	private final MessagesTable mMessagesTable;
	private final XMPPService mXMPPService;

	/**
	 * HandleMessagesListener takes care of messages that could not been sent
	 * and are therefore stored in the database for later submission.
	 * 
	 */
	public HandleMessagesListener(XMPPService xmppService) {
		mMessagesTable = MessagesTable.getInstance(xmppService.getContext());
		mXMPPService = xmppService;
	}

	@Override
	public void connected(Connection connection) {
		List<Entry> entries = mMessagesTable.getAllAndDelete();
		for (Entry e : entries)
			mXMPPService.send(e.mMessage, e.mOrigin);

	};
}
