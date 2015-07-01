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

package org.projectmaxs.shared.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.messagecontent.ContactNumber;
import org.projectmaxs.shared.global.util.PackageManagerUtil;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;

public class ContactUtil {

	public static final String CONTACTS_MODULE_PACKAGE = GlobalConstants.MODULE_PACKAGE
			+ ".contactsread";

	public static final Uri CONTACTS_MODULE_AUTHORITY = Uri.parse("content://"
			+ CONTACTS_MODULE_PACKAGE);

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

	private static final String AND = " AND ";
	private static final String LIMIT = " LIMIT";
	private static final String LIMIT_1 = LIMIT + " 1";

	public static Uri maxsContactUriFrom(Uri uri) {
		String pathSegment = uri.getEncodedPath();
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

	public boolean contactsReadModuleInstalled() {
		return PackageManagerUtil.getInstance(mContext).isPackageInstalled(CONTACTS_MODULE_PACKAGE);
	}

	/**
	 * Magical method that tries to find contacts based on a given String.
	 * 
	 * Only returns null if the contacts module is not installed or on error.
	 * 
	 * @param info
	 * @return A collection of matching contacts.
	 */
	public Collection<Contact> lookupContacts(String info) {
		if (!contactsReadModuleInstalled()) return null;

		String cleanNumber = ContactNumber.cleanNumber(info);
		if (ContactNumber.isNumber(cleanNumber)) return contactsByNumber(cleanNumber);

		Collection<Contact> contacts = contactsByNickname(info);
		if (contacts != null && contacts.size() > 0) return contacts;

		return contactsByName(info);
	}

	/**
	 * Lookup exactly one contact for a given number.
	 * 
	 * @param number
	 * @return the contact, or null if none was found or the contactsread module is not installed.
	 */
	public Contact contactByNumber(String number) {
		if (!contactsReadModuleInstalled()) return null;

		number = ContactNumber.cleanNumber(number);
		if (!ContactNumber.isNumber(number)) return null;

		Uri uri = Uri.withAppendedPath(MAXS_PHONE_LOOKUP_CONTENT_FILTER_URI, Uri.encode(number));
		final String[] projection = new String[] { PhoneLookup.LOOKUP_KEY, DISPLAY_NAME };
		Cursor c = mContentResolver.query(uri, projection, null, null, null);

		Contact contact = null;
		if (c.moveToFirst()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			String lookupKey = c.getString(c.getColumnIndexOrThrow(PhoneLookup.LOOKUP_KEY));
			contact = new Contact(displayName, lookupKey);
			lookupContactNumbersFor(contact);
		}
		c.close();

		return contact;
	}

	/**
	 * Get all contacts for a given number
	 * 
	 * @param number
	 * @return All contacts with that number.
	 */
	public Collection<Contact> contactsByNumber(String number) {
		if (!contactsReadModuleInstalled()) return null;

		number = ContactNumber.cleanNumber(number);
		if (!ContactNumber.isNumber(number)) return null;

		Uri uri = Uri.withAppendedPath(MAXS_PHONE_LOOKUP_CONTENT_FILTER_URI, Uri.encode(number));
		final String[] projection = new String[] { PhoneLookup.LOOKUP_KEY, DISPLAY_NAME };
		Cursor c = mContentResolver.query(uri, projection, null, null, null);

		Map<String, Contact> contactMap = new HashMap<String, Contact>();
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			String lookupKey = c.getString(c.getColumnIndexOrThrow(PhoneLookup.LOOKUP_KEY));

			Contact contact = contactMap.get(lookupKey);
			if (contact == null) {
				contact = new Contact(displayName, lookupKey);
				contactMap.put(lookupKey, contact);
				lookupContactNumbersFor(contact);
			}
		}
		c.close();

		return contactMap.values();
	}

	/**
	 * Lookup a contact by a given nickname.
	 * 
	 * The returned contact will come with all known contact numbers and a
	 * lookup key.
	 * 
	 * @param nickname
	 * @return A contact, or null if none found or on error.
	 */
	public Contact contactByNickname(String nickname) {
		if (!contactsReadModuleInstalled()) return null;

		final String[] projection = new String[] { Data.LOOKUP_KEY, DISPLAY_NAME, Nickname.NAME };
		final String selection = Nickname.NAME + "=?" + AND + Data.MIMETYPE + "='"
				+ Nickname.CONTENT_ITEM_TYPE + "'" + LIMIT_1;
		final String[] selectionArgs = new String[] { nickname };
		Cursor c = mContentResolver.query(MAXS_DATA_CONTENT_URI, projection, selection,
				selectionArgs, null);

		Contact contact = null;
		if (c.moveToFirst()) {
			String lookupKey = c.getString(c.getColumnIndexOrThrow(Data.LOOKUP_KEY));
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			nickname = c.getString(c.getColumnIndexOrThrow(Nickname.NAME));
			contact = new Contact(displayName, lookupKey);
			contact.setNickname(nickname);
			lookupContactNumbersFor(contact);
		}
		c.close();

		return contact;
	}

	/**
	 * Get all matching contacts for a given nickname.
	 * 
	 * The contacts will come with all known contact numbers and a lookup key.
	 * 
	 * @param nickname
	 * @return A contact, or null if none found or on error.
	 */
	public Collection<Contact> contactsByNickname(String nickname) {
		if (!contactsReadModuleInstalled()) return null;

		final String[] projection = new String[] { Data.LOOKUP_KEY, DISPLAY_NAME, Nickname.NAME };
		final String selection = Nickname.NAME + "=?" + AND + Data.MIMETYPE + "='"
				+ Nickname.CONTENT_ITEM_TYPE + "'";
		final String[] selectionArgs = new String[] { nickname };
		Cursor c = mContentResolver.query(MAXS_DATA_CONTENT_URI, projection, selection,
				selectionArgs, null);

		Collection<Contact> contacts = new ArrayList<Contact>();
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String lookupKey = c.getString(c.getColumnIndexOrThrow(Data.LOOKUP_KEY));
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			nickname = c.getString(c.getColumnIndexOrThrow(Nickname.NAME));
			Contact contact = new Contact(displayName, lookupKey);
			contact.setNickname(nickname);
			lookupContactNumbersFor(contact);
			contacts.add(contact);
		}
		c.close();

		return contacts;
	}

	/**
	 * Get a contact for a given name
	 * 
	 * The contact will come with all known contact numbers and a lookup key.
	 * 
	 * @param name
	 * @return A contact, or null if none found, contactsread is not installed or on error.
	 */
	public Contact contactByName(String name) {
		if (!contactsReadModuleInstalled()) return null;

		Uri uri = Uri.withAppendedPath(MAXS_CONTACTS_CONTENT_FILTER_URI, Uri.encode(name));
		final String[] projection = new String[] { Contacts.LOOKUP_KEY, DISPLAY_NAME };
		Cursor c = mContentResolver.query(uri, projection, LIMIT_1, null, null);

		Contact contact = null;
		if (c.moveToFirst()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			String lookupKey = c.getString(c.getColumnIndexOrThrow(Data.LOOKUP_KEY));
			contact = new Contact(displayName, lookupKey);
			lookupContactNumbersFor(contact);
		}

		c.close();
		return contact;
	}

	/**
	 * Get all known contacts for a given name
	 * 
	 * The contacts will come with all known contact numbers and a lookup key.
	 * 
	 * @param name
	 * @return A collection of matching Contacts or null if contactsread is not installed.
	 */
	public Collection<Contact> contactsByName(String name) {
		if (!contactsReadModuleInstalled()) return null;

		Uri uri = Uri.withAppendedPath(MAXS_CONTACTS_CONTENT_FILTER_URI, Uri.encode(name));
		final String[] projection = new String[] { Contacts.LOOKUP_KEY, DISPLAY_NAME };
		Cursor c = mContentResolver.query(uri, projection, null, null, null);

		Collection<Contact> res = new ArrayList<Contact>();
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String displayName = c.getString(c.getColumnIndexOrThrow(DISPLAY_NAME));
			String lookupKey = c.getString(c.getColumnIndexOrThrow(Data.LOOKUP_KEY));
			Contact contact = new Contact(displayName, lookupKey);
			lookupContactNumbersFor(contact);
			res.add(contact);
		}

		c.close();
		return res;
	}

	/**
	 * Lookup the numbers for a given contact.
	 * 
	 * Usually this is not needed because most methods already return the
	 * contacts with all known contact numbers. Make sure to call
	 * {@link #contactsReadModuleInstalled()} first.
	 * 
	 * @param contact
	 */
	public void lookupContactNumbersFor(Contact contact) {
		if (!contactsReadModuleInstalled()) return;

		String lookupKey = contact.getLookupKey();
		// @formatter:off
		final String[] projection = new String[] { 
				Phone.NUMBER,
				Phone.TYPE,
				Phone.LABEL,
				Phone.IS_SUPER_PRIMARY
				};
		// @formatter:on
		final String selection = ContactsContract.PhoneLookup.LOOKUP_KEY + "=?" + AND
				+ ContactsContract.Data.MIMETYPE + "='" + Phone.CONTENT_ITEM_TYPE + "'";
		final String[] selectionArgs = new String[] { lookupKey };
		Cursor c = mContentResolver.query(MAXS_DATA_CONTENT_URI, projection, selection,
				selectionArgs, null);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String number = c.getString(c
					.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
			int type = c.getInt(c
					.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE));
			String label = c.getString(c
					.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL));
			boolean superPrimary = c
					.getInt(c
							.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY)) > 0 ? true
					: false;
			contact.addNumber(number, type, label, superPrimary);
		}
		c.close();
	}

	/**
	 * Pretty print for a given contact and contactInfo. If contact is null, only contactInfo will
	 * be returned. Otherwise {@code"<contact.getDisplayName()> (<contactInfo>)"} will get
	 * returned.
	 * 
	 * @param contactInfo
	 * @param contact
	 * @return The contact as String
	 */
	public static String prettyPrint(String contactInfo, Contact contact) {
		if (contact == null) return contactInfo;
		String displayName = contact.getDisplayName();
		// Contacts can be saved without a name, i.e. just a number as "contact".
		return (displayName == null ? "unknown" : displayName) + " (" + contactInfo + ")";
	}

	/**
	 * If the given collection contains only one contact with a number, then this contact is
	 * returned. Otherwise, if more then one contact with a number exists or if none exists, null is
	 * returned.
	 * 
	 * @param contacts
	 * @return the one and only contact with number(s) from contacts or null
	 */
	public static Contact getOnlyContactWithNumber(Collection<Contact> contacts) {
		Contact res = null;
		for (Contact contact : contacts) {
			if (contact.hasNumbers()) {
				if (res != null) {
					return null;
				} else {
					res = contact;
				}
			}
		}
		return res;
	}

}
