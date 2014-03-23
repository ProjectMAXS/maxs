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

	public static final String AUTHORITY = GlobalConstants.MAIN_PACKAGE;

	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	public static final String RECENT_CONTACT_PATH = "recent_contact";

	public static final String OUTGOING_FILETRANSFER_PATH = "outgoing_filetransfer";

	public static final Uri RECENT_CONTACT_URI = Uri.withAppendedPath(AUTHORITY_URI,
			RECENT_CONTACT_PATH);

	public static final Uri OUTGOING_FILE_TRANSFER_URI = Uri.withAppendedPath(AUTHORITY_URI,
			OUTGOING_FILETRANSFER_PATH);

	public static final String CONTACT_INFO = "contact_info";
	public static final String LOOKUP_KEY = ContactsContract.Contacts.LOOKUP_KEY;
	public static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;

	public static final String[] RECENT_CONTACT_COLUMNS = new String[] { CONTACT_INFO, LOOKUP_KEY,
			DISPLAY_NAME };

	public static final String OUTGOING_FILETRANSFER_SERVICE = "outgoing_filetransfer";
	public static final String RECEIVER_INFO = "receiver_info";
	public static final String OUTGOING_FILESTRANSFER_PACKAGE = "outgoing_filetransfer_package";

	public static final String[] OUTGOING_FILETRANSFER_COLUMNS = new String[] {
			OUTGOING_FILETRANSFER_SERVICE, RECEIVER_INFO, OUTGOING_FILESTRANSFER_PACKAGE };
}
