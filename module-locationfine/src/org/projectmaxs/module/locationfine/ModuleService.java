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

package org.projectmaxs.module.locationfine;

import java.util.HashSet;
import java.util.Set;

import org.projectmaxs.module.locationfine.commands.LocateOnce;
import org.projectmaxs.module.locationfine.commands.LocateStart;
import org.projectmaxs.module.locationfine.commands.LocateStop;
import org.projectmaxs.module.locationfine.commands.LocationLastKnown;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SupraCommand;

import android.content.Context;

public class ModuleService extends MAXSModuleIntentService {

	public static final String LOCATIONFINE_MODULE_PACKAGE = GlobalConstants.MODULE_PACKAGE
			+ ".locationfine";

	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-locationfine", sCOMMANDS);
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			LOCATIONFINE_MODULE_PACKAGE,      // Package of the Module
			"MAXS Module LocationFine"
			);
	// @formatter:on

	public static final SupraCommand LOCATE = new SupraCommand("locate", "l");

	public static final SupraCommand[] sCOMMANDS;

	static {
		Set<SupraCommand> commands = new HashSet<SupraCommand>();

		SupraCommand.register(LocateStart.class, commands);
		SupraCommand.register(LocateStop.class, commands);
		SupraCommand.register(LocateOnce.class, commands);
		SupraCommand.register(LocationLastKnown.class, commands);

		sCOMMANDS = commands.toArray(new SupraCommand[commands.size()]);
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}
}
