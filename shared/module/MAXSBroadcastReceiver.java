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

package org.projectmaxs.shared.module;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class MAXSBroadcastReceiver extends BroadcastReceiver {

	private static final Log LOG = Log.getLog();

	@Override
	public void onReceive(Context context, Intent intent) {
		Message message = onReceiveReturnMessages(context, intent);
		if (message == null) {
			LOG.e("onReceive: message was null");
			return;
		}

		Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_MESSAGE);
		replyIntent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
		context.startService(replyIntent);
	}

	public abstract Message onReceiveReturnMessages(Context context, Intent intent);

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
