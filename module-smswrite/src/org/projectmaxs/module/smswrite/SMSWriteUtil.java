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

package org.projectmaxs.module.smswrite;

import org.projectmaxs.shared.global.messagecontent.Sms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class SMSWriteUtil {

	/**
	 * com.android.providers.telephony.SmsProvider.NOTIFICATION_URI
	 */
	public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");

	public static final Uri SMS_SENT_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "sent");

	public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");

	public static final Uri SMS_CONVERSATIONS_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "conversations");

	public static final void addSmsToSentBox(Sms sms, Context context) {
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("address", sms.getContact());
		values.put("date", sms.getDate());
		values.put("body", sms.getBody());
		resolver.insert(SMS_SENT_CONTENT_URI, values);
	}

	public static final long markAsRead(String address, Context context) {
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put("read", "1");
		return resolver.update(SMS_INBOX_CONTENT_URI, values, "read=0 AND address=?", new String[] { address });
	}

	// TODO delete (by number, by conversation id, by in-/out-box etc.)
}
