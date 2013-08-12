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

package org.projectmaxs.shared.global;

import java.io.File;

import android.os.Environment;

public class GlobalConstants {
	public static final String PACKAGE = "org.projectmaxs";
	public static final String MAIN_PACKAGE = PACKAGE + ".main";
	public static final String TRANSPORT_PACKAGE = PACKAGE + ".transport";

	public static final String ACTION_REGISTER = PACKAGE + ".REGISTER";
	public static final String ACTION_PERFORM_COMMAND = PACKAGE + ".PERFORM_COMMAND";
	public static final String ACTION_EXPORT_SETTINGS = PACKAGE + ".EXPORT_SETTINGS";
	public static final String ACTION_IMPORT_SETTINGS = PACKAGE + ".IMPORT_SETTINGS";
	public static final String ACTION_INCOMING_FILETRANSFER = PACKAGE + ".INCOMING_FILETRANSFER";
	public static final String ACTION_OUTGOING_FILETRANSFER = PACKAGE + ".OUTGOING_FILETRANSFER";
	public static final String ACTION_BIND_FILEREAD = PACKAGE + ".ACTION_BIND_FILEREAD";
	public static final String ACTION_BIND_FILEWRITE = PACKAGE + ".ACTION_BIND_FILEWRITE";

	public static final String ACTION_BIND_SERVICE = MAIN_PACKAGE + ".BIND_SERVICE";
	public static final String ACTION_REGISTER_MODULE = MAIN_PACKAGE + ".REGISTER_MODULE";
	public static final String ACTION_SET_RECENT_CONTACT = MAIN_PACKAGE + ".SET_RECENT_CONTACT";
	public static final String ACTION_UPDATE_STATUS = MAIN_PACKAGE + ".UPDATE_STATUS";
	public static final String ACTION_SEND_MESSAGE = MAIN_PACKAGE + ".SEND_MESSAGE";
	public static final String ACTION_EXPORT_TO_FILE = MAIN_PACKAGE + ".EXPORT_TO_FILE";
	public static final String ACTION_IMPORT_EXPORT_STATUS = MAIN_PACKAGE + ".IMPORT_EXPORT_STATUS";

	public static final String EXTRA_MODULE_INFORMATION = PACKAGE + ".MODULE_INFORMATION";
	public static final String EXTRA_COMMAND = PACKAGE + ".COMMAND";
	public static final String EXTRA_MESSAGE = PACKAGE + ".MESSAGE";
	public static final String EXTRA_FILE = PACKAGE + ".FILE";
	public static final String EXTRA_CONTENT = PACKAGE + ".CONTENT";
	public static final String EXTRA_PACKAGE = PACKAGE + ".PACKAGE";

	public static final String PERMISSION = PACKAGE + ".permission";
	public static final String PERMISSION_USE_MODULE = PERMISSION + ".USE_MODULE";
	public static final String PERMISSION_USE_TRANSPORT = PERMISSION + ".USE_TRANSPORT";
	public static final String PERMISSION_USE_MAIN = PERMISSION + ".USE_MAIN";
	public static final String PERMISSION_USE_MAIN_AS_MODULE = PERMISSION + ".USE_MAIN_AS_MODULE";
	public static final String PERMISSION_USE_MAIN_AS_TRANSPORT = PERMISSION + ".USE_MAIN_AS_TRANSPORT";

	public static final File MAXS_EXTERNAL_STORAGE = new File(Environment.getExternalStorageDirectory(), "MAXS");
}
