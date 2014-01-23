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

package org.projectmaxs.module.fileread;

import java.util.HashSet;
import java.util.Set;

import org.projectmaxs.module.fileread.commands.CdPath;
import org.projectmaxs.module.fileread.commands.CdTilde;
import org.projectmaxs.module.fileread.commands.LsPath;
import org.projectmaxs.module.fileread.commands.LsTilde;
import org.projectmaxs.module.fileread.commands.SendPath;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SupraCommand;

import android.content.Context;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-fileread", sCOMMANDS);
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.fileread",        // Package of the Module
			"fileread"                                // Name of the Module (if omitted, last substring after '.' is used)
			);
	// @formatter:on

	public static final SupraCommand CD = new SupraCommand("cd");
	public static final SupraCommand LS = new SupraCommand("ls");
	public static final SupraCommand SEND = new SupraCommand("send");

	public static final SupraCommand[] sCOMMANDS;

	static {
		Set<SupraCommand> commands = new HashSet<SupraCommand>();

		SupraCommand.register(CdPath.class, commands);
		SupraCommand.register(CdTilde.class, commands);
		SupraCommand.register(LsPath.class, commands);
		SupraCommand.register(LsTilde.class, commands);
		SupraCommand.register(SendPath.class, commands);

		sCOMMANDS = commands.toArray(new SupraCommand[commands.size()]);
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}

}
