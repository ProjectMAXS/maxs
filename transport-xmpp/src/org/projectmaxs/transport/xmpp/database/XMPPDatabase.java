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

import org.projectmaxs.transport.xmpp.util.Constants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class XMPPDatabase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = Constants.PACKAGE + ".db";

	public static final String TEXT_TYPE = " TEXT";
	public static final String TIMESTAMP_TYPE = " TIMESTAMP";
	public static final String INTEGER_TYPE = " INTEGER";
	public static final String BLOB_TYPE = " BLOB";
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
	public static final String NOT_NULL = " NOT NULL";
	public static final String COMMA_SEP = ",";
	public static final String SEMICOLON_SEP = ";";

	// @formatter:off
	private static final String[] SQL_CREATE_ENTRIES = new String[] {
				XMPPEntityCapsTable.CREATE_TABLE,
				MessagesTable.CREATE_TABLE, 
	};
	private static final String[] SQL_DELETE_ENTRIES = new String[] {
		XMPPEntityCapsTable.DELETE_TABLE,
		MessagesTable.DELETE_TABLE,
	};
	// @formatter:on

	private static XMPPDatabase sXMPPDatabase;

	protected static XMPPDatabase getInstance(Context context) {
		if (sXMPPDatabase == null) sXMPPDatabase = new XMPPDatabase(context);
		return sXMPPDatabase;
	}

	private XMPPDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		deleteTables(db);
		createTables(db);
	}

	private static void createTables(SQLiteDatabase db) {
		for (String s : SQL_CREATE_ENTRIES) {
			db.execSQL(s + SEMICOLON_SEP);
		}
	}

	private static void deleteTables(SQLiteDatabase db) {
		for (String s : SQL_DELETE_ENTRIES) {
			db.execSQL(s + SEMICOLON_SEP);
		}
	}
}
