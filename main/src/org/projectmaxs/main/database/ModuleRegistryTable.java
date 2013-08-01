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

import java.util.ArrayList;
import java.util.List;

import org.projectmaxs.main.util.ParcelableUtil;
import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.shared.util.Log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;

public class ModuleRegistryTable {

	private static final String TABLE_NAME = "moduleRegistry";
	private static final String COLUMN_NAME_MODULE_PACKAGE = "package";
	private static final String COLUMN_NAME_MODULE_INFORMATION = "information";
	private static final Log LOG = Log.getLog();

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " + TABLE_NAME +
		" (" +
		 COLUMN_NAME_MODULE_PACKAGE + MAXSDatabase.TEXT_TYPE + " PRIMARY KEY" + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_MODULE_INFORMATION + MAXSDatabase.BLOB_TYPE + MAXSDatabase.NOT_NULL +
		" )";
	// @formatter:on

	public static final String DELETE_TABLE = MAXSDatabase.DROP_TABLE + TABLE_NAME;

	private static ModuleRegistryTable sModuleRegistryTable;

	public static ModuleRegistryTable getInstance(Context context) {
		if (sModuleRegistryTable == null) sModuleRegistryTable = new ModuleRegistryTable(context);
		return sModuleRegistryTable;
	}

	private final SQLiteDatabase mDatabase;

	private ModuleRegistryTable(Context context) {
		mDatabase = MAXSDatabase.getInstance(context).getWritableDatabase();
	}

	public void insertOrReplace(ModuleInformation moduleInformation) {
		ContentValues values = new ContentValues();
		String modulePackage = moduleInformation.getModulePackage();
		values.put(COLUMN_NAME_MODULE_PACKAGE, modulePackage);
		values.put(COLUMN_NAME_MODULE_INFORMATION, ParcelableUtil.marshall(moduleInformation));

		long res = mDatabase.replace(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert ModuleInformation in database");
	}

	public boolean containsModule(String modulePackage) {
		Cursor c = mDatabase.query(TABLE_NAME, null, COLUMN_NAME_MODULE_PACKAGE + "='" + modulePackage + "'", null,
				null, null, null);
		boolean exists = c.moveToFirst();
		c.close();
		return exists;
	}

	public List<ModuleInformation> getAll() {
		List<ModuleInformation> res = new ArrayList<ModuleInformation>();
		Cursor c = mDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		if (!c.moveToFirst()) {
			c.close();
			return res;
		}

		do {
			byte[] moduleInformationMarshalled = c.getBlob(c.getColumnIndex(COLUMN_NAME_MODULE_INFORMATION));
			Parcel parcel = ParcelableUtil.unmarshall(moduleInformationMarshalled);
			// ModuleInformation moduleInformation =
			// ModuleInformation.CREATOR.createFromParcel(parcel);
			ModuleInformation moduleInformation = new ModuleInformation(parcel);
			res.add(moduleInformation);
		} while (c.moveToNext());

		c.close();
		return res;
	}

	public int deleteModuleInformation(String packageName) {
		return mDatabase.delete(TABLE_NAME, COLUMN_NAME_MODULE_PACKAGE + "='" + packageName + "'", null);
	}
}
