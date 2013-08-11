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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class XMPPEntityCapsTable {

	private static final String TABLE_NAME = "xmppEntityCaps";
	private static final String COLUMN_NAME_NODE = "node";
	private static final String COLUMN_NAME_INFO = "timestamp";

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		" (" +
		 COLUMN_NAME_NODE + MAXSDatabase.TEXT_TYPE + " PRIMARY KEY" + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_INFO + MAXSDatabase.TEXT_TYPE + MAXSDatabase.NOT_NULL +
		" )";
	// @formatter:on

	public static final String DELETE_TABLE = MAXSDatabase.DROP_TABLE + TABLE_NAME;

	private static XMPPEntityCapsTable sXMPPEntityCapsTable;

	public static XMPPEntityCapsTable getInstance(Context context) {
		if (sXMPPEntityCapsTable == null) sXMPPEntityCapsTable = new XMPPEntityCapsTable(context);
		return sXMPPEntityCapsTable;
	}

	private final SQLiteDatabase mDatabase;

	private XMPPEntityCapsTable(Context context) {
		mDatabase = MAXSDatabase.getInstance(context).getWritableDatabase();
	}

	public void addDiscoverInfo(String node, String info) {
		if (containsNode(node)) return;

		ContentValues values = new ContentValues();
		values.put(COLUMN_NAME_NODE, node);
		values.put(COLUMN_NAME_INFO, info);

		long res = mDatabase.insert(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert discover info in database");
	}

	public Map<String, String> getDiscoverInfos() {
		Map<String, String> res = new HashMap<String, String>();
		Cursor c = mDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		if (!c.moveToFirst()) return res;

		do {
			String info = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_INFO));
			String node = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_NODE));
			res.put(node, info);
		} while (c.moveToNext());

		c.close();
		return res;
	}

	public boolean containsNode(String node) {
		Cursor c = mDatabase.query(TABLE_NAME, null, COLUMN_NAME_NODE + "='" + node + "'", null, null, null, null);
		boolean exists = c.moveToFirst();
		c.close();
		return exists;
	}

	public void emptyTable() {
		mDatabase.delete(TABLE_NAME, null, null);
	}
}
