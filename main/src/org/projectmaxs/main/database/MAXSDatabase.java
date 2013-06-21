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

import org.projectmaxs.main.util.Constants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MAXSDatabase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = Constants.MAIN_PACKAGE + ".db";

	public static final String TEXT_TYPE = " TEXT";
	public static final String TIMESTAMP_TYPE = " TIMESTAMP";
	public static final String INTEGER_TYPE = " INTEGER";
	public static final String BLOB_TYPE = " BLOB";
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
	public static final String NOT_NULL = " NOT NULL";
	public static final String COMMA_SEP = ",";
	public static final String SEMICOLON_SEP = ";";

	// @formatter:off
	private static final String SQL_CREATE_ENTRIES =
				CommandTable.CREATE_TABLE + SEMICOLON_SEP
			+	ModuleRegistryTable.CREATE_TABLE + SEMICOLON_SEP
			+	XMPPEntityCapsTable.CREATE_TABLE + SEMICOLON_SEP
			+	XMPPMessageTable.CREATE_TABLE + SEMICOLON_SEP;
	// @formatter:on

	private static MAXSDatabase sMAXSDatabase;

	protected static MAXSDatabase getInstance(Context context) {
		if (sMAXSDatabase == null) sMAXSDatabase = new MAXSDatabase(context);
		return sMAXSDatabase;
	}

	private MAXSDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(CommandTable.DELETE_TABLE);
		db.execSQL(CommandTable.CREATE_TABLE);
		db.execSQL(ModuleRegistryTable.DELETE_TABLE);
		db.execSQL(ModuleRegistryTable.CREATE_TABLE);
		db.execSQL(XMPPEntityCapsTable.DELETE_TABLE);
		db.execSQL(XMPPEntityCapsTable.CREATE_TABLE);
		db.execSQL(XMPPMessageTable.DELETE_TABLE);
		db.execSQL(XMPPMessageTable.CREATE_TABLE);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

}
