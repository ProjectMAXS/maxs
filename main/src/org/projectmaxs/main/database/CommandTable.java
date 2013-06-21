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

import java.sql.Timestamp;

import org.projectmaxs.main.MAXSService.CommandOrigin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

public class CommandTable {
	private static final long OLD_ENTRIES_AGE = 1000 * 60 * 60 * 24 * 3;

	private static final String TABLE_NAME = "commands";
	private static final String COLUMN_NAME_COMMAND_ID = "commandId";
	private static final String COLUMN_NAME_TIMESTAMP = "timestamp";
	private static final String COLUMN_NAME_COMMAND = "command";
	private static final String COLUMN_NAME_SUBCOMMAND = "subcommand";
	private static final String COLUMN_NAME_ARGS = "args";
	private static final String COLUMN_NAME_ORIGN = "orign";
	private static final String COLUMN_NAME_ORIGIN_ISSUER_INFO = "originIssuerInfo";
	private static final String COLUMN_NAME_ORIGIN_ID = "originId";

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		" (" +
		 COLUMN_NAME_COMMAND_ID + MAXSDatabase.INTEGER_TYPE + " PRIMARY KEY" + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_TIMESTAMP + MAXSDatabase.TIMESTAMP_TYPE + MAXSDatabase.NOT_NULL + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_COMMAND + MAXSDatabase.TEXT_TYPE + MAXSDatabase.NOT_NULL + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_SUBCOMMAND + MAXSDatabase.TEXT_TYPE + MAXSDatabase.NOT_NULL + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_ARGS + MAXSDatabase.TEXT_TYPE + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_ORIGN + MAXSDatabase.INTEGER_TYPE + MAXSDatabase.NOT_NULL + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_ORIGIN_ISSUER_INFO + MAXSDatabase.TEXT_TYPE + MAXSDatabase.NOT_NULL + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_ORIGIN_ID + MAXSDatabase.TEXT_TYPE +
		" )";
	// @formatter:on

	public static final String DELETE_TABLE = MAXSDatabase.DROP_TABLE + TABLE_NAME;

	private static CommandTable sCommandTable;

	public static CommandTable getInstance(Context context) {
		if (sCommandTable == null) sCommandTable = new CommandTable(context);
		return sCommandTable;
	}

	private final SQLiteDatabase mDatabase;
	private final Handler mHandler;

	private CommandTable(Context context) {
		mDatabase = MAXSDatabase.getInstance(context).getWritableDatabase();
		mHandler = new Handler();
		purgeOldEntries();
	}

	public void addCommand(int id, String command, String subCmd, String args, CommandOrigin origin,
			String issuerInformation, String originId) {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_COMMAND_ID, id);
		values.put(COLUMN_NAME_TIMESTAMP, timestamp.toString());
		values.put(COLUMN_NAME_COMMAND, command);
		values.put(COLUMN_NAME_SUBCOMMAND, subCmd);
		values.put(COLUMN_NAME_ARGS, args);
		values.put(COLUMN_NAME_ORIGN, origin.ordinal());
		values.put(COLUMN_NAME_ORIGIN_ISSUER_INFO, issuerInformation);
		values.put(COLUMN_NAME_ORIGIN_ID, originId);

		long res = mDatabase.insert(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert command in database");
	}

	public Entry geEntry(int id) {
		// @formatter:off
		String[] projection = { 
				COLUMN_NAME_ORIGN,
				COLUMN_NAME_ORIGIN_ISSUER_INFO,
				COLUMN_NAME_ORIGIN_ID
				};
		// @formatter:on
		Cursor c = mDatabase.query(TABLE_NAME, projection, COLUMN_NAME_COMMAND_ID + "='" + id + "'", null, null, null,
				null);
		if (!c.moveToFirst()) return null;

		int originInt = c.getInt(c.getColumnIndex(COLUMN_NAME_ORIGN));
		String originIssuerInfo = c.getString(c.getColumnIndex(COLUMN_NAME_ORIGIN_ISSUER_INFO));
		String originId = c.getString(c.getColumnIndex(COLUMN_NAME_ORIGIN_ID));

		CommandOrigin origin = CommandOrigin.values()[originInt];
		return new Entry(id, origin, originIssuerInfo, originId);
	}

	public Entry getFullEntry(int id) {
		Cursor c = mDatabase.query(TABLE_NAME, null, COLUMN_NAME_COMMAND_ID + "='" + id + "'", null, null, null, null);
		if (!c.moveToFirst()) return null;

		String timestampStr = c.getString(c.getColumnIndex(COLUMN_NAME_TIMESTAMP));
		String command = c.getString(c.getColumnIndex(COLUMN_NAME_COMMAND));
		String subCmd = c.getString(c.getColumnIndex(COLUMN_NAME_SUBCOMMAND));
		String args = c.getString(c.getColumnIndex(COLUMN_NAME_ARGS));
		int originInt = c.getInt(c.getColumnIndex(COLUMN_NAME_ORIGN));
		String originIssuerInfo = c.getString(c.getColumnIndex(COLUMN_NAME_ORIGIN_ISSUER_INFO));
		String originId = c.getString(c.getColumnIndex(COLUMN_NAME_ORIGIN_ID));

		Timestamp timestmap = Timestamp.valueOf(timestampStr);
		CommandOrigin origin = CommandOrigin.values()[originInt];
		return new FullEntry(id, timestmap, command, subCmd, args, origin, originIssuerInfo, originId);
	}

	public static class Entry {
		public final int mId;
		public final CommandOrigin mOrigin;
		public final String mOriginIssuerInfo;
		public final String mOriginId;

		Entry(int id, CommandOrigin origin, String originIssuerInfo, String originId) {
			this.mId = id;
			this.mOrigin = origin;
			this.mOriginIssuerInfo = originIssuerInfo;
			this.mOriginId = originId;
		}
	}

	public static class FullEntry extends Entry {
		public final Timestamp mTimestamp;
		public final String mCommand;
		public final String mSubCmd;
		public final String mArgs;

		FullEntry(int id, Timestamp timestamp, String command, String subCmd, String args, CommandOrigin origin,
				String originIssuerInfo, String originId) {
			super(id, origin, originIssuerInfo, originId);
			this.mTimestamp = timestamp;
			this.mCommand = command;
			this.mSubCmd = subCmd;
			this.mArgs = args;

		}
	}

	private void purgeOldEntries() {
		String oldTimestamp = (new Timestamp(System.currentTimeMillis() - OLD_ENTRIES_AGE)).toString();
		mDatabase.delete(TABLE_NAME, COLUMN_NAME_TIMESTAMP + " < " + oldTimestamp, null);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				purgeOldEntries();
			}
		}, OLD_ENTRIES_AGE);
	}
}
