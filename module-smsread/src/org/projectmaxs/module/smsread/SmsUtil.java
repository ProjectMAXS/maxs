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

package org.projectmaxs.module.smsread;

import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.shared.global.messagecontent.Sms;
import org.projectmaxs.shared.global.util.Log;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class SmsUtil {

	private static final Log LOG = Log.getLog();
	private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	private static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");
	private static final Uri SMS_SENTBOX_CONTENT_URI = Uri
			.withAppendedPath(SMS_CONTENT_URI, "sent");

	public static final List<Sms> getOrderedSMS(String selection, String[] selectionArgs,
			int maxResults, Context context) {
		List<Sms> res = new LinkedList<Sms>();
		final String[] projection = new String[] { "address", "body", "date", "type" };
		String sortOrder = "date DESC";
		if (maxResults > 0) sortOrder += " limit " + maxResults;
		Cursor c = context.getContentResolver().query(SMS_CONTENT_URI, projection, selection,
				selectionArgs, sortOrder);
		if (c == null) {
			LOG.w("getOrderedSMS: cursor was null");
			return res;
		}

		if (!c.moveToFirst()) return res;
		do {
			String address = c.getString(c.getColumnIndexOrThrow("address"));
			int type = c.getInt(c.getColumnIndexOrThrow("type"));
			String body = c.getString(c.getColumnIndexOrThrow("body"));
			long date = c.getLong(c.getColumnIndexOrThrow("date"));
			res.add(new Sms(address, body, getType(type), date));
		} while (c.moveToNext());

		return res;
	}

	public static final Sms.Type getType(int type) {
		// from android.provider.Telephony.TextBasedSmsColumns
		switch (type) {
		case 0:
			return Sms.Type.ALL;
		case 1:
			return Sms.Type.INBOX;
		case 2:
			return Sms.Type.SENT;
		case 3:
			return Sms.Type.DRAFT;
		case 4:
			return Sms.Type.OUTBOX;
		case 5:
			return Sms.Type.FAILED;
		case 6:
			return Sms.Type.QUEUED;
		default:
			throw new IllegalStateException();
		}
	}
}
