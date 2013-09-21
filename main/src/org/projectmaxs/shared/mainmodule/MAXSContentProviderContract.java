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

import org.projectmaxs.shared.global.GlobalConstants;

import android.net.Uri;
import android.provider.ContactsContract;

public class MAXSContentProviderContract {

	public static final Uri AUTHORITY_URI = Uri.parse("content://" + GlobalConstants.MAIN_PACKAGE);

	public static final Uri RECENT_CONTACT_URI = Uri.withAppendedPath(AUTHORITY_URI,
			"recent_contact");

	public static final String CONTACT_INFO = "contact_info";
	public static final String LOOKUP_KEY = ContactsContract.Contacts.LOOKUP_KEY;
	public static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;

	public static final String[] RECENT_CONTACT_COLUMNS = new String[] { CONTACT_INFO, LOOKUP_KEY,
			DISPLAY_NAME };
}
