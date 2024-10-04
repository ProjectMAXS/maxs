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

import org.projectmaxs.main.database.CommandTable;
import org.projectmaxs.main.database.CommandTable.Entry;
import org.projectmaxs.shared.mainmodule.MAXSContentProviderContract;
import org.projectmaxs.shared.mainmodule.RecentContact;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class MAXSContentProvider extends ContentProvider {

	private static final UriMatcher sUriMatcher;

	private static final int RECENT_CONTACT = 1;

	private static final int OUTGOING_FILETRANSFER = 2;

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		MatrixCursor c;
		switch (sUriMatcher.match(uri)) {
		case RECENT_CONTACT:
			c = new MatrixCursor(MAXSContentProviderContract.RECENT_CONTACT_COLUMNS, 1);
			RecentContact recentContact = MAXSService.getRecentContact();
			if (recentContact == null) return c;

			String contactInfo = recentContact.mContactInfo;
			String lookupKey = null;
			String displayName = null;
			if (recentContact.mContact != null) {
				lookupKey = recentContact.mContact.getLookupKey();
				displayName = recentContact.mContact.getDisplayName();
			}
			c.addRow(new Object[] { contactInfo, lookupKey, displayName });
			break;
		case OUTGOING_FILETRANSFER:
			int cmdId = Integer.valueOf(uri.getPathSegments().get(1));
			Entry entry = CommandTable.getInstance(getContext()).getEntry(cmdId);
			String pkg = entry.mOrigin.getPackage();
			String service = TransportRegistry.getInstance(getContext())
					.getFiletransferService(pkg);
			String receiverInfo = entry.mOrigin.getOriginIssuerInfo();
			c = new MatrixCursor(MAXSContentProviderContract.OUTGOING_FILETRANSFER_COLUMNS);
			c.addRow(new Object[] { service, receiverInfo, pkg });
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		return c;
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

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(MAXSContentProviderContract.AUTHORITY,
				MAXSContentProviderContract.RECENT_CONTACT_PATH, RECENT_CONTACT);
		sUriMatcher.addURI(MAXSContentProviderContract.AUTHORITY,
				MAXSContentProviderContract.OUTGOING_FILETRANSFER_PATH + "/#",
				OUTGOING_FILETRANSFER);

	}

}
