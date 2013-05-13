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

package org.projectmaxs.shared;

public class GlobalConstants {
	public static final String PACKAGE = "org.projectmaxs";
	public static final String MAIN_PACKAGE = PACKAGE + ".main";
	public static final String ACTION_REGISTER = PACKAGE + ".REGISTER";
	public static final String ACTION_PERFORM_COMMAND = PACKAGE + ".PERFORM_COMMAND";
	public static final String ACTION_BIND_SERVICE = MAIN_PACKAGE + ".BIND_SERVICE";
	public static final String ACTION_REGISTER_MODULE = MAIN_PACKAGE + ".REGISTER_MODULE";
	public static final String ACTION_SET_RECENT_CONTACT = MAIN_PACKAGE + ".SET_RECENT_CONTACT";
	public static final String ACTION_UPDATE_XMPP_STATUS = MAIN_PACKAGE + ".UPDATE_XMPP_STATUS";
	public static final String ACTION_SEND_XMPP_MESSAGE = MAIN_PACKAGE + ".SEND_XMPP_MESSAGE";

	public static final String EXTRA_MODULE_INFORMATION = PACKAGE + ".MODULE_INFORMATION";
	public static final String EXTRA_COMMAND = PACKAGE + ".COMMAND";
	public static final String EXTRA_XMPP_MESSAGE = PACKAGE + ".XMPP_MESSAGE";

	public static final String PERMISSON = PACKAGE + ".permission";
	public static final String PERMISSON_USE_MAIN = PERMISSON + ".USE_MAIN";
	public static final String PERMISSON_USE_MODULE = PERMISSON + ".USE_MODULE";

	public static final String DELIVER_COUNT = PACKAGE + ".INTENT_DELIVER_COUNT";

}
