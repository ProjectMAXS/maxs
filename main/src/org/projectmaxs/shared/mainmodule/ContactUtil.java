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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.PackageManagerUtil;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;

public class ContactUtil {

	public static final String CONTACTS_MODULE_PACKAGE = "org.projectmaxs.module.contacts";
	public static final Uri CONTACTS_MODULE_AUTHORITY = Uri.parse("content://" + CONTACTS_MODULE_PACKAGE);
	public static final Uri MAXS_PHONE_LOOKUP_CONTENT_FILTER_URI = Uri.withAppendedPath(CONTACTS_MODULE_AUTHORITY,
			"phone_lookup");

	private static final Log LOG = Log.getLog();

	private static ContactUtil sContactUtil;

	public static synchronized ContactUtil getInstance(Context context) {
		if (sContactUtil == null) sContactUtil = new ContactUtil(context);
		return sContactUtil;
	}

	private ContactUtil(Context context) {
		mContext = context;
	}

	private final Context mContext;

	public boolean contactsModuleInstalled() {
		return PackageManagerUtil.getInstance(mContext).isPackageInstalled(CONTACTS_MODULE_PACKAGE);
	}

	/**
	 * Lookup exactly one contact for a given number
	 * 
	 * @param phoneNumber
	 * @return
	 */
	public Contact lookupContact(String phoneNumber) {
		if (!contactsModuleInstalled()) return null;
		if (!ContactNumber.isNumber(phoneNumber)) return null;

		Uri uri = Uri.withAppendedPath(MAXS_PHONE_LOOKUP_CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		final String[] projection = new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup.NUMBER, PhoneLookup.TYPE,
				PhoneLookup.LABEL };
		Cursor c = mContext.getContentResolver().query(uri, projection, null, null, null);

		if (c == null) {
			LOG.e("lookupContact: returned cursor is null");
			return null;
		}

		Contact res = null;
		if (c.moveToFirst()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
			String number = c.getString(c.getColumnIndexOrThrow(PhoneLookup.NUMBER));
			int type = c.getInt(c.getColumnIndexOrThrow(PhoneLookup.TYPE));
			String label = c.getString(c.getColumnIndexOrThrow(PhoneLookup.LABEL));
			res = new Contact(displayName);
			res.addNumber(number, type, label);
		}
		c.close();

		return res;
	}

	/**
	 * Get all contacts for a given number
	 * 
	 * @param phoneNumber
	 * @return
	 */
	public Collection<Contact> lookupContacts(String phoneNumber) {
		if (!contactsModuleInstalled()) return null;
		if (!ContactNumber.isNumber(phoneNumber)) return null;

		Uri uri = Uri.withAppendedPath(MAXS_PHONE_LOOKUP_CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		final String[] projection = new String[] { PhoneLookup.LOOKUP_KEY, PhoneLookup.DISPLAY_NAME };
		Cursor c = mContext.getContentResolver().query(uri, projection, null, null, null);

		if (c == null) {
			LOG.e("lookupContact: returned cursor is null");
			return null;
		}

		Map<String, Contact> contactMap = new HashMap<String, Contact>();
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
			String lookupKey = c.getString(c.getColumnIndexOrThrow(PhoneLookup.LOOKUP_KEY));
			String number = c.getString(c.getColumnIndexOrThrow(PhoneLookup.NUMBER));
			int type = c.getInt(c.getColumnIndexOrThrow(PhoneLookup.TYPE));
			String label = c.getString(c.getColumnIndexOrThrow(PhoneLookup.LABEL));

			Contact contact = contactMap.get(lookupKey);
			if (contact == null) {
				contact = new Contact(displayName, lookupKey);
				contactMap.put(lookupKey, contact);
			}
			contact.addNumber(number, type, label);
		}
		c.close();

		return contactMap.values();
	}

}
