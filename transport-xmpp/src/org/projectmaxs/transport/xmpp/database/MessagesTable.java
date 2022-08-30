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

package org.projectmaxs.transport.xmpp.database;

import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.ParcelableUtil;
import org.projectmaxs.shared.maintransport.CommandOrigin;
import org.projectmaxs.transport.xmpp.util.Constants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Don't get confused by the name of the Table. It's meant for XMPP messages and IQ packets.
 * 
 */
public class MessagesTable {

	private static final String TABLE_NAME = "messages";
	private static final String COLUMN_NAME_MESSAGE = "message";
	private static final String COLUMN_NAME_INTENT_ACTION = "intentAction";
	private static final String COLUMN_NAME_ISSUER_INFO = "issuerInfo";
	private static final String COLUMN_NAME_ISSUER_ID = "issuerId";

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		" (" +
		 COLUMN_NAME_INTENT_ACTION + XMPPDatabase.TEXT_TYPE + XMPPDatabase.NOT_NULL + XMPPDatabase.COMMA_SEP +
		 COLUMN_NAME_ISSUER_INFO + XMPPDatabase.TEXT_TYPE + XMPPDatabase.COMMA_SEP +
		 COLUMN_NAME_ISSUER_ID + XMPPDatabase.TEXT_TYPE + XMPPDatabase.COMMA_SEP +
		 COLUMN_NAME_MESSAGE + XMPPDatabase.BLOB_TYPE +
		" )";
	// @formatter:on

	public static final String DELETE_TABLE = XMPPDatabase.DROP_TABLE + TABLE_NAME;

	private static MessagesTable sXMPPMessageTable;

	public static MessagesTable getInstance(Context context) {
		if (sXMPPMessageTable == null) sXMPPMessageTable = new MessagesTable(context);
		return sXMPPMessageTable;
	}

	private final SQLiteDatabase mDatabase;

	private MessagesTable(Context context) {
		mDatabase = XMPPDatabase.getInstance(context).getWritableDatabase();
	}

	public void addMessage(Message message, String intentAction, String issuerId, String issuerInfo) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_MESSAGE, ParcelableUtil.marshall(message));
		values.put(COLUMN_NAME_INTENT_ACTION, intentAction);
		values.put(COLUMN_NAME_ISSUER_INFO, issuerInfo);
		values.put(COLUMN_NAME_ISSUER_ID, issuerId);

		long res = mDatabase.insert(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert command in database");
	}

	public List<Entry> getAllAndDelete() {
		List<Entry> entries = new LinkedList<Entry>();
		Cursor c = mDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		if (!c.moveToFirst()) {
			c.close();
			return entries;
		}

		do {
			byte[] messageBytes = c.getBlob(c.getColumnIndexOrThrow(COLUMN_NAME_MESSAGE));
			Message message = ParcelableUtil.unmarshall(messageBytes, Message.CREATOR, true);
			if (message != null) {
				String intentAction = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_INTENT_ACTION));
				String issuerId = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_ISSUER_ID));
				String issuerInfo = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_ISSUER_INFO));
				entries.add(new Entry(message, new CommandOrigin(Constants.PACKAGE, intentAction,
						issuerInfo, issuerId)));
			}
		} while (c.moveToNext());

		// Delete all rows with the given origin after we have read out the
		// values
		mDatabase.delete(TABLE_NAME, null, null);

		c.close();
		return entries;
	}

	public static class Entry {
		public final Message mMessage;
		public final CommandOrigin mOrigin;

		private Entry(Message message, CommandOrigin origin) {
			mMessage = message;
			mOrigin = origin;
		}
	}
}
