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

package org.projectmaxs.module.contactsread;

import org.projectmaxs.shared.global.CrossProcessCursorWrapper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class MAXSContactProvider extends ContentProvider {

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		throw new IllegalStateException("MAXSContacts is a read-only provider");
	}

	@Override
	public String getType(Uri uri) {
		ContentResolver contentResolver = getContext().getContentResolver();
		return contentResolver.getType(getContactsProviderUri(uri));
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new IllegalStateException("MAXSContacts is a read-only provider");
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		ContentResolver contentResolver = getContext().getContentResolver();
		Cursor cursor = contentResolver.query(getContactsProviderUri(uri), projection, selection,
				selectionArgs, sortOrder);
		return new CrossProcessCursorWrapper(cursor);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new IllegalStateException("MAXSContacts is a read-only provider");
	}

	private static Uri getContactsProviderUri(Uri uri) {
		String pathSegment = uri.getPath();
		return Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, pathSegment);
	}
}
