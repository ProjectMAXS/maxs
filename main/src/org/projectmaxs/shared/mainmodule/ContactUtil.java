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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.PackageManagerUtil;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;

public class ContactUtil {

	public static final String CONTACTS_MODULE_PACKAGE = "org.projectmaxs.module.contacts";
	public static final Uri CONTACTS_MODULE_AUTHORITY = Uri.parse("content://" + CONTACTS_MODULE_PACKAGE);

	/**
	 * ContactsContract.PhoneLookup.CONTENT_FILTER_URI
	 */
	public static final Uri MAXS_PHONE_LOOKUP_CONTENT_FILTER_URI = maxsContactUriFrom(ContactsContract.PhoneLookup.CONTENT_FILTER_URI);

	/**
	 * ContactsContract.Data.CONTENT_URI
	 */
	public static final Uri MAXS_DATA_CONTENT_URI = maxsContactUriFrom(ContactsContract.Data.CONTENT_URI);

	/**
	 * ContactsContract.Contacts.CONTENT_URI
	 */
	public static final Uri MAXS_CONTACTS_CONTENT_URI = maxsContactUriFrom(ContactsContract.Contacts.CONTENT_URI);

	/**
	 * ContactsContract.Contacts.CONTENT_LOOKUP_URI
	 */
	public static final Uri MAXS_CONTACTS_CONTENT_LOOKUP_URI = maxsContactUriFrom(ContactsContract.Contacts.CONTENT_LOOKUP_URI);

	/**
	 * ContactsContract.Contacts.CONTENT_FILTER_URI
	 */
	public static final Uri MAXS_CONTACTS_CONTENT_FILTER_URI = maxsContactUriFrom(ContactsContract.Contacts.CONTENT_FILTER_URI);

	private static final Log LOG = Log.getLog();

	public static Uri maxsContactUriFrom(Uri uri) {
		String pathSegment = uri.getPath();
		return Uri.withAppendedPath(CONTACTS_MODULE_AUTHORITY, pathSegment);
	}

	@SuppressLint("InlinedApi")
	private static final String DISPLAY_NAME = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Contacts.DISPLAY_NAME_PRIMARY
			: Contacts.DISPLAY_NAME;

	private static ContactUtil sContactUtil;

	public static synchronized ContactUtil getInstance(Context context) {
		if (sContactUtil == null) sContactUtil = new ContactUtil(context);
		return sContactUtil;
	}

	private ContactUtil(Context context) {
		mContext = context;
		mContentResolver = context.getContentResolver();
	}

	private final Context mContext;
	private final ContentResolver mContentResolver;

	public boolean contactsModuleInstalled() {
		return PackageManagerUtil.getInstance(mContext).isPackageInstalled(CONTACTS_MODULE_PACKAGE);
	}

	/**
	 * Lookup exactly one contact for a given number
	 * 
	 * @param phoneNumber
	 * @return
	 */
	public Contact contactByNumber(String number) {
		if (!contactsModuleInstalled()) return null;

		number = ContactNumber.cleanNumber(number);
		if (!ContactNumber.isNumber(number)) return null;

		Uri uri = Uri.withAppendedPath(MAXS_PHONE_LOOKUP_CONTENT_FILTER_URI, Uri.encode(number));
		final String[] projection = new String[] { DISPLAY_NAME, PhoneLookup.NUMBER, PhoneLookup.TYPE,
				PhoneLookup.LABEL };
		Cursor c = mContentResolver.query(uri, projection, null, null, null);

		if (c == null) {
			LOG.e("contactByNumber: returned cursor is null");
			return null;
		}

		Contact contact = null;
		if (c.moveToFirst()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			String num = c.getString(c.getColumnIndexOrThrow(PhoneLookup.NUMBER));
			int type = c.getInt(c.getColumnIndexOrThrow(PhoneLookup.TYPE));
			String label = c.getString(c.getColumnIndexOrThrow(PhoneLookup.LABEL));
			contact = new Contact(displayName);
			contact.addNumber(num, type, label);
		}
		c.close();

		return contact;
	}

	/**
	 * Get all contacts for a given number
	 * 
	 * @param phoneNumber
	 * @return
	 */
	public Collection<Contact> contactsByNumber(String number) {
		if (!contactsModuleInstalled()) return null;

		number = ContactNumber.cleanNumber(number);
		if (!ContactNumber.isNumber(number)) return null;

		Uri uri = Uri.withAppendedPath(MAXS_PHONE_LOOKUP_CONTENT_FILTER_URI, Uri.encode(number));
		final String[] projection = new String[] { PhoneLookup.LOOKUP_KEY, DISPLAY_NAME, PhoneLookup.NUMBER,
				PhoneLookup.TYPE, PhoneLookup.LABEL };
		Cursor c = mContentResolver.query(uri, projection, null, null, null);

		if (c == null) {
			LOG.e("contactsByNumber: returned cursor is null");
			return null;
		}

		Map<String, Contact> contactMap = new HashMap<String, Contact>();
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			String lookupKey = c.getString(c.getColumnIndexOrThrow(PhoneLookup.LOOKUP_KEY));
			String num = c.getString(c.getColumnIndexOrThrow(PhoneLookup.NUMBER));
			int type = c.getInt(c.getColumnIndexOrThrow(PhoneLookup.TYPE));
			String label = c.getString(c.getColumnIndexOrThrow(PhoneLookup.LABEL));

			Contact contact = contactMap.get(lookupKey);
			if (contact == null) {
				contact = new Contact(displayName, lookupKey);
				contactMap.put(lookupKey, contact);
			}
			contact.addNumber(num, type, label);
		}
		c.close();

		return contactMap.values();
	}

	public Contact contactByNickname(String nickname) {
		Uri uri = Uri.withAppendedPath(MAXS_DATA_CONTENT_URI, Uri.encode(nickname));
		final String[] projection = new String[] { ContactsContract.Data.LOOKUP_KEY, DISPLAY_NAME };
		final String selection = ContactsContract.CommonDataKinds.Nickname.DATA + "=?";
		Cursor c = mContentResolver.query(uri, projection, selection, new String[] { nickname }, null);

		if (c == null) {
			LOG.e("contactByNickname: returned cursor is null");
			return null;
		}

		Contact contact = null;
		if (c.moveToFirst()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			String lookupKey = c.getString(c.getColumnIndexOrThrow(ContactsContract.Data.LOOKUP_KEY));

			contact = new Contact(displayName, lookupKey);
			contact.setNickname(nickname);
			lookupContactNumbersFor(contact);
		}
		c.close();

		return contact;
	}

	public Collection<Contact> contactsNyNickname(String nickname) {
		Uri uri = Uri.withAppendedPath(MAXS_DATA_CONTENT_URI, Uri.encode(nickname));
		final String[] projection = new String[] { PhoneLookup.LOOKUP_KEY, DISPLAY_NAME };
		final String selection = ContactsContract.CommonDataKinds.Nickname.DATA + "=?";
		final String[] selectionArgs = new String[] { nickname };
		Cursor c = mContentResolver.query(uri, projection, selection, selectionArgs, null);

		if (c == null) {
			LOG.e("contactByNickname: returned cursor is null");
			return null;
		}

		Collection<Contact> res = new ArrayList<Contact>();
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			String lookupKey = c.getString(c.getColumnIndexOrThrow(PhoneLookup.LOOKUP_KEY));

			Contact contact = new Contact(displayName, lookupKey);
			contact.setNickname(nickname);
			lookupContactNumbersFor(contact);
			res.add(contact);
		}
		c.close();

		return res;
	}

	public Contact contactByName(String name) {
		Uri uri = Uri.withAppendedPath(MAXS_CONTACTS_CONTENT_FILTER_URI, Uri.encode(name));
		final String[] projection = new String[] { ContactsContract.Contacts.LOOKUP_KEY, DISPLAY_NAME };
		Cursor c = mContentResolver.query(uri, projection, null, null, null);

		Contact contact = null;
		if (c.moveToFirst()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			String lookupKey = c.getString(c.getColumnIndexOrThrow(ContactsContract.Data.LOOKUP_KEY));
			contact = new Contact(displayName, lookupKey);
			lookupContactNumbersFor(contact);
		}

		c.close();
		return contact;
	}

	public Collection<Contact> contactsByName(String name) {
		Uri uri = Uri.withAppendedPath(MAXS_CONTACTS_CONTENT_FILTER_URI, Uri.encode(name));
		final String[] projection = new String[] { ContactsContract.Contacts.LOOKUP_KEY, DISPLAY_NAME };
		Cursor c = mContentResolver.query(uri, projection, null, null, null);

		Collection<Contact> res = new ArrayList<Contact>();
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			String lookupKey = c.getString(c.getColumnIndexOrThrow(ContactsContract.Data.LOOKUP_KEY));
			Contact contact = new Contact(displayName, lookupKey);
			lookupContactNumbersFor(contact);
			res.add(contact);
		}

		c.close();
		return res;
	}

	public void lookupContactNumbersFor(Contact contact) {
		String lookupKey = contact.getLookupKey();
		Uri uri = Uri.withAppendedPath(MAXS_CONTACTS_CONTENT_LOOKUP_URI, lookupKey);
		final String[] projection = new String[] { PhoneLookup.NUMBER, PhoneLookup.TYPE, PhoneLookup.LABEL };
		final String selection = ContactsContract.PhoneLookup.LOOKUP_KEY + "=?";
		final String[] selectionArgs = new String[] { lookupKey };
		Cursor c = mContentResolver.query(uri, projection, selection, selectionArgs, null);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String number = c.getString(c.getColumnIndexOrThrow(PhoneLookup.NUMBER));
			int type = c.getInt(c.getColumnIndexOrThrow(PhoneLookup.TYPE));
			String label = c.getString(c.getColumnIndexOrThrow(PhoneLookup.LABEL));
			contact.addNumber(number, type, label);
		}
		c.close();
	}

}
