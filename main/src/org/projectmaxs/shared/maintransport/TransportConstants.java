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

package org.projectmaxs.shared.maintransport;

import org.projectmaxs.shared.global.GlobalConstants;

public class TransportConstants {

	public static final String TRANSPORT_PACKAGE = GlobalConstants.TRANSPORT_PACKAGE;

	public static final String TRANSPORT_SERVICE = ".TransportService";

	public static final String ACTION_START_SERVICE = TRANSPORT_PACKAGE + ".START_SERVICE";
	public static final String ACTION_STOP_SERVICE = TRANSPORT_PACKAGE + ".STOP_SERVICE";
	public static final String ACTION_SET_STATUS = TRANSPORT_PACKAGE + ".SET_STATUS";
	public static final String ACTION_REGISTER_TRANSPORT = TRANSPORT_PACKAGE + ".REGISTER_TRANSPORT";

	public static final String EXTRA_ORIGIN_ISSUER_INFO = TRANSPORT_PACKAGE + ".ORIGIN_ISSUER_INFO";
	public static final String EXTRA_ORIGIN_ID = TRANSPORT_PACKAGE + ".ORIGIN_ID";
	public static final String EXTRA_TRANSPORT_INFORMATION = TRANSPORT_PACKAGE + ".TRANSPORT_INFORMATION";

}
