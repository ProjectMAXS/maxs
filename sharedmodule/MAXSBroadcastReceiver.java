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

package org.projectmaxs.sharedmodule;

import java.util.List;

import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.Message;
import org.projectmaxs.shared.MessageContent;
import org.projectmaxs.shared.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class MAXSBroadcastReceiver extends BroadcastReceiver {

	private static final Log LOG = Log.getLog();

	@Override
	public void onReceive(Context context, Intent intent) {
		android.os.Debug.waitForDebugger();
		List<MessageContent> messages = onReceiveReturnMessages(context, intent);
		if (messages == null) {
			LOG.e("onReceive: messages was null");
			return;
		}
		if (messages.isEmpty()) {
			LOG.e("onReceive: messages is empty");
			return;
		}

		for (MessageContent mc : messages) {
			Message message = new Message(mc);

			Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_USER_MESSAGE);
			replyIntent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
			context.startService(replyIntent);
		}
	}

	public abstract List<MessageContent> onReceiveReturnMessages(Context context, Intent intent);

	public void setRecentContact(Context context, String contactNumber) {
		if (contactNumber == null) {
			LOG.e("setRecentContact: contactNumber was null");
			return;
		}
		final Intent intent = new Intent(GlobalConstants.ACTION_SET_RECENT_CONTACT);
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, contactNumber);
		context.startService(intent);
	}

}
