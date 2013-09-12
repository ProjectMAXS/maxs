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

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.mainmodule.MAXSContentProviderContract;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class RecentContactUtil {

	/**
	 * Set the recent contact info.
	 * 
	 * Not that if a Contact is provided, all information but the display name
	 * and lookup key will get stripped.
	 * 
	 * @param usedContactInfo
	 *            some sort of string (e.g. number, e-mail address), most be
	 *            provided
	 * @param contact
	 *            the contact with lookup key, optional
	 * @param context
	 */
	public static void setRecentContact(String recentContactInfo, Contact contact, Context context) {
		if (recentContactInfo == null) throw new IllegalArgumentException("recentContactInfo must not be null");

		final Intent intent = new Intent(GlobalConstants.ACTION_SET_RECENT_CONTACT);
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, recentContactInfo);
		intent.putExtra(GlobalConstants.EXTRA_CONTACT, contact.getMinimal());
		context.startService(intent);
	}

	/**
	 * Note that it may be possible that the lookup key of the Contact changed.
	 * But we assume that the key is stable most of the time.
	 * 
	 * @param context
	 * @return
	 */
	public static RecentContact getRecentContact(Context context) {
		Cursor c = context.getContentResolver().query(MAXSContentProviderContract.RECENT_CONTACT_URI, null, null, null,
				null);
		if (c == null) return null;
		if (!c.moveToFirst()) throw new IllegalStateException("Recent contact cursor was empty");

		String contactInfo = c.getString(c.getColumnIndexOrThrow(MAXSContentProviderContract.CONTACT_INFO));
		String lookupKey = c.getString(c.getColumnIndex(MAXSContentProviderContract.LOOKUP_KEY));
		String displayName = c.getString(c.getColumnIndexOrThrow(MAXSContentProviderContract.DISPLAY_NAME));

		Contact contact = new Contact(displayName, lookupKey);
		return new RecentContact(contactInfo, contact);
	}

	public static class RecentContact {
		public RecentContact(String contactInfo, Contact contact) {
			mContactInfo = contactInfo;
			mContact = contact;
		}

		public final String mContactInfo;
		public final Contact mContact;
	}
}
