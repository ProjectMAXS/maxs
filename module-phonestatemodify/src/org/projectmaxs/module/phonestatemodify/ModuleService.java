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

package org.projectmaxs.module.phonestatemodify;

import java.util.HashSet;
import java.util.Set;

import org.projectmaxs.module.phonestatemodify.commands.DataDisable;
import org.projectmaxs.module.phonestatemodify.commands.DataEnable;
import org.projectmaxs.module.phonestatemodify.commands.PhoneReject;
import org.projectmaxs.module.phonestatemodify.commands.PinSupply;
import org.projectmaxs.module.phonestatemodify.commands.RadioOff;
import org.projectmaxs.module.phonestatemodify.commands.RadioOn;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SupraCommand;

import android.content.Context;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-phonestatemodify", sCOMMANDS);
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.phonestatemodify",      // Package of the Module
			"MAXS Module Bluetooth"                  // Name of the Module
			);
	// @formatter:on

	public static final SupraCommand sREJECT = new SupraCommand("reject", "rej");
	public static final SupraCommand sPIN = new SupraCommand("pin");
	public static final SupraCommand sDATA = new SupraCommand("data");
	public static final SupraCommand sRADIO = new SupraCommand("radio");

	public static final SupraCommand[] sCOMMANDS;

	static {
		Set<SupraCommand> commands = new HashSet<SupraCommand>();

		SupraCommand.register(DataDisable.class, commands);
		SupraCommand.register(DataEnable.class, commands);
		SupraCommand.register(PhoneReject.class, commands);
		SupraCommand.register(PinSupply.class, commands);
		SupraCommand.register(RadioOff.class, commands);
		SupraCommand.register(RadioOn.class, commands);

		sCOMMANDS = commands.toArray(new SupraCommand[commands.size()]);
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}
}
