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

import org.projectmaxs.shared.ModuleInformation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;

public class ModuleRegistryTable {

	private static final String TABLE_NAME = "moduleRegistry";
	private static final String COLUMN_NAME_MODULE_PACKAGE = "package";
	private static final String COLUMN_NAME_MODULE_INFORMATION = "information";

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		" (" +
		 COLUMN_NAME_MODULE_PACKAGE + MAXSDatabase.TEXT_TYPE + " PRIMARY KEY" + MAXSDatabase.COMMA_SEP +
		 COLUMN_NAME_MODULE_INFORMATION + MAXSDatabase.BLOB_TYPE + MAXSDatabase.NOT_NULL + MAXSDatabase.COMMA_SEP +
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

	public void addModuleInformation(ModuleInformation moduleInformation) {
		ContentValues values = new ContentValues();
		String modulePackage = moduleInformation.getModulePackage();
		Parcel parcel = Parcel.obtain();
		moduleInformation.writeToParcel(parcel, 0);
		byte[] moduleInformationMarshalled = parcel.marshall();
		values.put(COLUMN_NAME_MODULE_PACKAGE, modulePackage);
		values.put(COLUMN_NAME_MODULE_INFORMATION, moduleInformationMarshalled);

		long res = mDatabase.insert(TABLE_NAME, null, values);
		if (res == -1) throw new IllegalStateException("Could not insert command in database");
	}

	public ModuleInformation getModuleInformation(String modulePackage) {
		Cursor c = mDatabase.query(TABLE_NAME, null, COLUMN_NAME_MODULE_PACKAGE + "='" + modulePackage + "'", null,
				null, null, null);
		if (!c.moveToFirst()) return null;

		byte[] moduleInformationMarshalled = c.getBlob(c.getColumnIndex(COLUMN_NAME_MODULE_INFORMATION));
		Parcel parcel = Parcel.obtain();
		parcel.unmarshall(moduleInformationMarshalled, 0, moduleInformationMarshalled.length);
		ModuleInformation moduleInformation = ModuleInformation.CREATOR.createFromParcel(parcel);

		return moduleInformation;
	}

	public int deleteModuleInformation(String modulePackage) {
		return mDatabase.delete(TABLE_NAME, COLUMN_NAME_MODULE_PACKAGE + "='" + modulePackage + "'", null);
	}
}
