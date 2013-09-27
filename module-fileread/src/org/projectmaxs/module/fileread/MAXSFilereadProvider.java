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

package org.projectmaxs.module.fileread;

import org.projectmaxs.shared.module.FilereadUtil;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * 
 * ContentProvider of the fileread module. Used to get the current working directory. Note that we
 * could have used insert() to set the cwd, but instead the cwd is set with a service.
 * 
 */
public class MAXSFilereadProvider extends ContentProvider {

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		final MatrixCursor cursor = new MatrixCursor(FilereadUtil.FILEREAD_PROVIDER_COLUMN_NAMES, 1);
		final Object[] row = new String[] { Settings.getInstance(getContext()).getCwd()
				.getAbsolutePath() };
		cursor.addRow(row);
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new IllegalStateException("MAXSFileread is a read-only provider");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new IllegalStateException("MAXSFileread is a read-only provider");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new IllegalStateException("MAXSFileread is a read-only provider");
	}

}
