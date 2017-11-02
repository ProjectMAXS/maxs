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

import org.projectmaxs.shared.global.StatusInformation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StatusTable {
	private static final String TABLE_NAME = "status";
	private static final String COLUMN_NAME_KEY = "key";
	private static final String COLUMN_NAME_HUMAN_STATUS = "humanStatus";
	private static final String COLUMN_NAME_MACHINE_STATUS = "machineStatus";

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		" (" +
		 COLUMN_NAME_KEY + MAXSDatabase.TEXT_TYPE + " PRIMARY KEY" + ',' +
		 COLUMN_NAME_HUMAN_STATUS + MAXSDatabase.TEXT_TYPE + ',' +
		 COLUMN_NAME_MACHINE_STATUS + MAXSDatabase.TEXT_TYPE + MAXSDatabase.NOT_NULL +
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
		values.put(COLUMN_NAME_HUMAN_STATUS, info.getHumanValue());
		values.put(COLUMN_NAME_MACHINE_STATUS, info.getMachineValue());

		long res = mDatabase.replace(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert status info in database");
	}

	public Map<String, StatusInformation> getAll() {
		Map<String, StatusInformation> res = new HashMap<>();
		Cursor c = mDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		if (!c.moveToFirst()) {
			c.close();
			return res;
		}

		try {
			do {
				String key = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_KEY));
				String humanStatus = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_HUMAN_STATUS));
				String machineStatus = c
						.getString(c.getColumnIndexOrThrow(COLUMN_NAME_MACHINE_STATUS));
				StatusInformation statusInformation = new StatusInformation(key, humanStatus,
						machineStatus);
				res.put(key, statusInformation);
			} while (c.moveToNext());
		} finally {
			c.close();
		}

		return res;
	}

	public void emptyTable() {
		mDatabase.delete(TABLE_NAME, null, null);
	}
}
