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

package org.projectmaxs.transport.xmpp.util;

import org.projectmaxs.shared.global.GlobalConstants;

public class Constants {

	public static final String PACKAGE = GlobalConstants.TRANSPORT_PACKAGE + ".xmpp";

	public static final String ACTION_SEND_AS_MESSAGE = PACKAGE + ".SEND_AS_MESSAGE";
	public static final String ACTION_SEND_AS_IQ = PACKAGE + ".SEND_AS_IQ";
	public static final String ACTION_NETWORK_TYPE_CHANGED = PACKAGE + ".NETWORK_TYPE_CHANGED";
	public static final String ACTION_NETWORK_CONNECTED = PACKAGE + ".NETWORK_CONNECTED";
	public static final String ACTION_NETWORK_DISCONNECTED = PACKAGE + ".NETWORK_DISCONNECTED";

}
