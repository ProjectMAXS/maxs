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

import java.util.HashMap;
import java.util.Map;

import org.projectmaxs.shared.StatusInformation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StatusTable {
	private static final String TABLE_NAME = "status";
	private static final String COLUMN_NAME_KEY = "key";
	private static final String COLUMN_NAME_STATUS = TABLE_NAME;

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		" (" +
		 COLUMN_NAME_KEY + MAXSDatabase.TEXT_TYPE + " PRIMARY KEY" + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_STATUS + MAXSDatabase.TEXT_TYPE + MAXSDatabase.NOT_NULL +
		" )";
	// @formatter:on

	public static final String DELETE_TABLE = MAXSDatabase.DROP_TABLE + TABLE_NAME;

	private static StatusTable sStatusTable;

	public static StatusTable getInstance(Context context) {
		if (sStatusTable == null) sStatusTable = new StatusTable(context);
		return sStatusTable;
	}

	private final SQLiteDatabase mDatabase;

	private StatusTable(Context context) {
		mDatabase = MAXSDatabase.getInstance(context).getWritableDatabase();
	}

	public void addStatus(StatusInformation info) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_KEY, info.getKey());
		values.put(COLUMN_NAME_STATUS, info.getValue());

		long res = mDatabase.replace(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert status info in database");
	}

	public Map<String, String> getAll() {
		Map<String, String> res = new HashMap<String, String>();
		Cursor c = mDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		if (!c.moveToFirst()) return res;

		do {
			String info = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_KEY));
			String node = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_STATUS));
			res.put(node, info);
		} while (c.moveToNext());

		c.close();
		return res;
	}

	public void emptyTable() {
		mDatabase.delete(TABLE_NAME, null, null);
	}
}
