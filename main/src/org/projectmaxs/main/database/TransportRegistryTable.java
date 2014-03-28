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

import org.projectmaxs.shared.global.util.ParcelableUtil;
import org.projectmaxs.shared.maintransport.TransportInformation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TransportRegistryTable {

	private static final String TABLE_NAME = "transportRegistry";
	private static final String COLUMN_NAME_TRANSPORT_PACKAGE = "package";
	private static final String COLUMN_NAME_TRANSPORT_INFORMATION = "information";

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " + TABLE_NAME +
		" (" +
		 COLUMN_NAME_TRANSPORT_PACKAGE + MAXSDatabase.TEXT_TYPE + " PRIMARY KEY" + ',' +
		 COLUMN_NAME_TRANSPORT_INFORMATION + MAXSDatabase.BLOB_TYPE + MAXSDatabase.NOT_NULL +
		" )";
	// @formatter:on

	public static final String DELETE_TABLE = MAXSDatabase.DROP_TABLE + TABLE_NAME;

	private static TransportRegistryTable sTransportRegistryTable;

	public static TransportRegistryTable getInstance(Context context) {
		if (sTransportRegistryTable == null)
			sTransportRegistryTable = new TransportRegistryTable(context);
		return sTransportRegistryTable;
	}

	private final SQLiteDatabase mDatabase;

	private TransportRegistryTable(Context context) {
		mDatabase = MAXSDatabase.getInstance(context).getWritableDatabase();
	}

	public void insertOrReplace(TransportInformation transportInformation) {
		ContentValues values = new ContentValues();
		String modulePackage = transportInformation.getTransportPackage();
		values.put(COLUMN_NAME_TRANSPORT_PACKAGE, modulePackage);
		values.put(COLUMN_NAME_TRANSPORT_INFORMATION, ParcelableUtil.marshall(transportInformation));

		long res = mDatabase.replace(TABLE_NAME, null, values);
		if (res == -1)
			throw new IllegalStateException("Could not insert TransportInformation in database");
	}

	public boolean containsTransport(String transportPackage) {
		Cursor c = mDatabase.query(TABLE_NAME, null, COLUMN_NAME_TRANSPORT_PACKAGE + "= ?",
				new String[] { transportPackage }, null, null, null);
		boolean exists = c.moveToFirst();
		c.close();
		return exists;
	}

	public List<TransportInformation> getAll() {
		List<TransportInformation> res = new LinkedList<TransportInformation>();
		Cursor c = mDatabase.query(TABLE_NAME, null, null, null, null, null, null);
		if (!c.moveToFirst()) {
			c.close();
			return res;
		}

		do {
			byte[] transportInformationMarshalled = c.getBlob(c
					.getColumnIndex(COLUMN_NAME_TRANSPORT_INFORMATION));
			TransportInformation transportInformation = ParcelableUtil.unmarshall(
					transportInformationMarshalled, TransportInformation.CREATOR);
			res.add(transportInformation);
		} while (c.moveToNext());

		c.close();
		return res;
	}

	public int deleteTransportInformation(String packageName) {
		return mDatabase.delete(TABLE_NAME, COLUMN_NAME_TRANSPORT_PACKAGE + "= ?",
				new String[] { packageName });
	}
}
