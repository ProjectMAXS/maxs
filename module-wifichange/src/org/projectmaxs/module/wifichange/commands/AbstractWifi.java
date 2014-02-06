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

package org.projectmaxs.module.wifichange.commands;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.ModuleConstants;
import org.projectmaxs.shared.module.SubCommand;

import android.content.Context;
import android.net.wifi.WifiManager;

public abstract class AbstractWifi extends SubCommand {

	WifiManager mWifiManager;

	public AbstractWifi(String command) {
		this(command, false);
	}

	public AbstractWifi(String command, boolean isDefaultWithoutArguments) {
		super(ModuleConstants.WIFI, command, isDefaultWithoutArguments);
	}

	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		if (mWifiManager == null)
			mWifiManager = (WifiManager) service.getSystemService(Context.WIFI_SERVICE);

		return null;

	}
}
