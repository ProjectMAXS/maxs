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

package org.projectmaxs.main;

import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.mainmodule.MAXSContentProviderContract;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class MAXSContentProvider extends ContentProvider {

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// Could use UriMatcher here. But since it's not really needed, got with
		// the simple approach.
		if (uri.equals(MAXSContentProviderContract.RECENT_CONTACT_URI)) {
			MatrixCursor c = new MatrixCursor(MAXSContentProviderContract.RECENT_CONTACT_COLUMNS, 1);
			Contact recentContact = MAXSService.getRecentContact();
			if (recentContact == null) return c;
			// If the recent contact is set, the it must always have also a
			// number attached with it. So no need to check getBestNumber() for
			// null
			String number = recentContact.getBestNumber(null).getNumber();
			String displayName = recentContact.getDisplayName();
			String lookupKey = recentContact.getLookupKey();
			c.addRow(new Object[] { number, displayName, lookupKey });
			return c;
		}

		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new IllegalStateException("MAXSContentProvider is a read-only provider");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new IllegalStateException("MAXSContentProvider is a read-only provider");
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new IllegalStateException("MAXSContentProvider is a read-only provider");
	}

}
