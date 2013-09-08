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

package org.projectmaxs.shared.mainmodule;

import java.util.ArrayList;
import java.util.List;

import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.PackageManagerUtil;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

public class ContactsResolver {

	public static final String CONTACTS_MODULE_PACKAGE = "org.projectmaxs.module.contacts";
	public static final Uri CONTACTS_MODULE_AUTHORITY = Uri.parse("content://" + CONTACTS_MODULE_PACKAGE);
	public static final Uri MAXS_PHONE_LOOKUP_CONTENT_FILTER_URI = Uri.withAppendedPath(CONTACTS_MODULE_AUTHORITY,
			"phone_lookup");

	private static final Log LOG = Log.getLog();

	private static ContactsResolver sContactsResolver;

	public static synchronized ContactsResolver getInstance(Context context) {
		if (sContactsResolver == null) sContactsResolver = new ContactsResolver(context);
		return sContactsResolver;
	}

	private ContactsResolver(Context context) {
		mContext = context;
	}

	private final Context mContext;

	public boolean contactsModuleInstalled() {
		return PackageManagerUtil.getInstance(mContext).isPackageInstalled(CONTACTS_MODULE_PACKAGE);
	}

	public List<Contact> lookupContact(String phoneNumber) {
		if (!contactsModuleInstalled()) return null;

		if (!ContactNumber.isNumber(phoneNumber)) throw new IllegalStateException("Not a phone number");

		Uri uri = Uri.withAppendedPath(MAXS_PHONE_LOOKUP_CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		final String[] projection = new String[] { PhoneLookup._ID };
		Cursor c = mContext.getContentResolver().query(uri, projection, null, null, null);

		if (c == null) {
			LOG.e("lookupContact: returned cursor is null");
			return null;
		}

		List<Contact> res = new ArrayList<Contact>();
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {

		}
		c.close();

		return null;
	}

}
