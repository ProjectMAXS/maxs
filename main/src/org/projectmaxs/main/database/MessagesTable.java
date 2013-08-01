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

package org.projectmaxs.main.database;

import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.main.MAXSService.CommandOrigin;
import org.projectmaxs.main.util.ParcelableUtil;
import org.projectmaxs.shared.Message;
import org.projectmaxs.shared.util.Log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MessagesTable {

	private static final Log LOG = Log.getLog();

	private static final String TABLE_NAME = "messages";
	private static final String COLUMN_NAME_MESSAGE = "message";
	private static final String COLUMN_NAME_ORIGIN = "origin";

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		" (" +
		 COLUMN_NAME_ORIGIN + MAXSDatabase.INTEGER_TYPE + MAXSDatabase.NOT_NULL + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_MESSAGE + MAXSDatabase.BLOB_TYPE +
		" )";
	// @formatter:on

	public static final String DELETE_TABLE = MAXSDatabase.DROP_TABLE + TABLE_NAME;

	private static MessagesTable sXMPPMessageTable;

	public static MessagesTable getInstance(Context context) {
		if (sXMPPMessageTable == null) sXMPPMessageTable = new MessagesTable(context);
		return sXMPPMessageTable;
	}

	private final SQLiteDatabase mDatabase;

	private MessagesTable(Context context) {
		mDatabase = MAXSDatabase.getInstance(context).getWritableDatabase();
	}

	public void addMessage(Message message, CommandOrigin origin) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_MESSAGE, ParcelableUtil.marshall(message));
		values.put(COLUMN_NAME_ORIGIN, origin.ordinal());

		long res = mDatabase.insert(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert command in database");
	}

	public List<Message> getAndDelete(CommandOrigin origin) {
		List<Message> messageList = new LinkedList<Message>();
		final String where = COLUMN_NAME_ORIGIN + "='" + origin.ordinal() + "'";
		Cursor c = mDatabase.query(TABLE_NAME, null, where, null, null, null, null);
		if (!c.moveToFirst()) return messageList;

		do {
			byte[] messageBytes = c.getBlob(c.getColumnIndex(COLUMN_NAME_MESSAGE));
			Message message = Message.CREATOR.createFromParcel(ParcelableUtil.unmarshall(messageBytes));
			messageList.add(message);
		} while (!c.moveToNext());

		// Delete all rows with the given origin after we have read out the
		// values
		mDatabase.delete(TABLE_NAME, where, null);

		c.close();
		return messageList;
	}
}
