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

package org.projectmaxs.module.misc;

import java.util.HashSet;
import java.util.Set;

import org.projectmaxs.module.misc.commands.ExceptionThrow;
import org.projectmaxs.module.misc.commands.LocationProviders;
import org.projectmaxs.module.misc.commands.Ping;
import org.projectmaxs.module.misc.commands.RecentcontactShow;
import org.projectmaxs.module.misc.commands.SensorList;
import org.projectmaxs.module.misc.commands.SysinfoMaxs;
import org.projectmaxs.module.misc.commands.SysinfoShow;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SupraCommand;

import android.content.Context;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-misc", sCOMMANDS);
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.misc",      // Package of the Module
			"MAXS Module Misc"                             // Name of the Module (if omitted, last substring after '.' is used)
			);
	// @formatter:on

	public static final SupraCommand SYSINFO = new SupraCommand("sysinfo");
	public static final SupraCommand EXCEPTION = new SupraCommand("exception");
	public static final SupraCommand RECENT_CONTACT = new SupraCommand("recentcontact", "recent");
	public static final SupraCommand SENSOR = new SupraCommand("sensor");
	public static final SupraCommand PING = new SupraCommand("ping");

	public static final SupraCommand[] sCOMMANDS;

	static {
		Set<SupraCommand> commands = new HashSet<SupraCommand>();

		SupraCommand.register(SysinfoShow.class, commands);
		SupraCommand.register(SysinfoMaxs.class, commands);
		SupraCommand.register(RecentcontactShow.class, commands);
		SupraCommand.register(ExceptionThrow.class, commands);
		SupraCommand.register(SensorList.class, commands);
		SupraCommand.register(LocationProviders.class, commands);
		SupraCommand.register(Ping.class, commands);

		sCOMMANDS = commands.toArray(new SupraCommand[commands.size()]);
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}
}
