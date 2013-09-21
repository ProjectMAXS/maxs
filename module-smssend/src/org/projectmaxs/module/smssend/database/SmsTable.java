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

package org.projectmaxs.module.smssend.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SmsTable {
	private static final String TABLE_NAME = "sms";
	private static final String COLUMN_NAME_CMD_ID = "cmdID";
	private static final String COLUMN_NAME_RECEIVER = "receiver";
	private static final String COLUMN_NAME_SHORT_TEXT = "shortText";
	private static final String COLUMN_NAME_PART_COUNT = "partCount";
	private static final String COLUMN_NAME_DELIVERED_INTENTS = "deliveredIntents";
	private static final String COLUMN_NAME_SENT_INTENTS = "sentIntents";

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		" (" +
		 COLUMN_NAME_CMD_ID + SMSSendDatabase.INTEGER_TYPE + " PRIMARY KEY" + SMSSendDatabase.COMMA_SEP +
		 COLUMN_NAME_RECEIVER + SMSSendDatabase.TEXT_TYPE + SMSSendDatabase.NOT_NULL + SMSSendDatabase.COMMA_SEP +
		 COLUMN_NAME_SHORT_TEXT + SMSSendDatabase.TEXT_TYPE + SMSSendDatabase.NOT_NULL + SMSSendDatabase.COMMA_SEP +
 		 COLUMN_NAME_PART_COUNT + SMSSendDatabase.INTEGER_TYPE + SMSSendDatabase.NOT_NULL + SMSSendDatabase.COMMA_SEP +
		 COLUMN_NAME_DELIVERED_INTENTS + SMSSendDatabase.TEXT_TYPE + SMSSendDatabase.COMMA_SEP +
		 COLUMN_NAME_SENT_INTENTS + SMSSendDatabase.TEXT_TYPE +
		" )";
	// @formatter:on

	public static final String DELETE_TABLE = SMSSendDatabase.DROP_TABLE + TABLE_NAME;

	private static SmsTable sSmsTable;

	public static SmsTable getInstance(Context context) {
		if (sSmsTable == null) sSmsTable = new SmsTable(context);
		return sSmsTable;
	}

	private final SQLiteDatabase mDatabase;

	private SmsTable(Context context) {
		mDatabase = SMSSendDatabase.getInstance(context).getWritableDatabase();
	}

	public void addSms(int cmdId, String receiver, String shortText, int partCount,
			boolean createSentIntents, boolean createDeliveredIntents) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_CMD_ID, cmdId);
		values.put(COLUMN_NAME_RECEIVER, receiver);
		values.put(COLUMN_NAME_SHORT_TEXT, shortText);
		values.put(COLUMN_NAME_PART_COUNT, partCount);
		if (createSentIntents) values.put(COLUMN_NAME_SENT_INTENTS, createIntentEntry(partCount));
		if (createDeliveredIntents)
			values.put(COLUMN_NAME_DELIVERED_INTENTS, createIntentEntry(partCount));

		long res = mDatabase.insert(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert status info in database");
	}

	public enum IntentType {
		SENT, DELIVERED,
	}

	public void updateIntents(int cmdId, String intents, IntentType intentType) {
		ContentValues values = new ContentValues();
		values.put(getColumnFor(intentType), intents);

		final String[] whereArgs = new String[] { Integer.toString(cmdId) };
		mDatabase.update(TABLE_NAME, values, COLUMN_NAME_CMD_ID + "=?", whereArgs);
	}

	public String getIntents(int cmdId, IntentType intentType) {
		final String intentColumn = getColumnFor(intentType);
		final String[] columns = new String[] { intentColumn };
		final String[] selectionArgs = new String[] { Integer.toString(cmdId) };
		Cursor c = mDatabase.query(TABLE_NAME, columns, COLUMN_NAME_CMD_ID + "=?", selectionArgs,
				null, null, null);

		String res = null;
		if (c.moveToFirst()) {
			res = c.getString(c.getColumnIndexOrThrow(intentColumn));
		}
		c.close();
		return res;
	}

	public void emptyTable() {
		mDatabase.delete(TABLE_NAME, null, null);
	}

	private static String createIntentEntry(int intentCount) {
		char[] charArray = new char[intentCount];
		for (int i = 0; i < charArray.length; i++)
			charArray[i] = '_';
		return new String(charArray);
	}

	private static String getColumnFor(IntentType intentType) {
		switch (intentType) {
		case SENT:
			return COLUMN_NAME_SENT_INTENTS;
		case DELIVERED:
			return COLUMN_NAME_DELIVERED_INTENTS;
		default:
			throw new IllegalStateException();
		}
	}
}
