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

public class ModuleConstants {

	public static final SupraCommand SMS = new SupraCommand("sms", "s");
	public static final SupraCommand BLUEOOTH = new SupraCommand("bluetooth", "bt");
	public static final SupraCommand CONTACT = new SupraCommand("contact", "c");
	public static final SupraCommand WIFI = new SupraCommand("wifi", "w");

	public static final String SMSWRITE_MODULE_PACKAGE = GlobalConstants.MODULE_PACKAGE
			+ ".smswrite";
	public static final String SMSWRITE_SERVICE = SMSWRITE_MODULE_PACKAGE + ".SMSWriteService";
}
