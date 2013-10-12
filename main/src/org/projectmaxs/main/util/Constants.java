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

package org.projectmaxs.main.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.projectmaxs.shared.global.GlobalConstants;

public class Constants {
	// @formatter:off
	public static final String[] COMPONENT_RECEIVERS = new String[] { 
		"ModuleReceiver",
		"TransportReceiver"
		};
	// @formatter:on

	public static final DateFormat ISO8601_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

	public static final String MAIN_PACKAGE = GlobalConstants.MAIN_PACKAGE;
	public static final String ACTION_START_SERVICE = MAIN_PACKAGE + ".START_SERVICE";
	public static final String ACTION_STOP_SERVICE = MAIN_PACKAGE + ".STOP_SERVICE";
}
