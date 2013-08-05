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

package org.projectmaxs.module.contacts;

import org.projectmaxs.shared.Command;
import org.projectmaxs.shared.MessageContent;
import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.shared.util.Log;
import org.projectmaxs.sharedmodule.MAXSModuleIntentService;

import android.content.Context;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-contacts");
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.contacts",      // Package of the Module
			"contacts",                             // Name of the Module (if omitted, last substring after '.' is used)
			new ModuleInformation.Command[] {        // Array of commands provided by the module
					new ModuleInformation.Command(
							"contacts",             // Command name
							"c",                    // Short command name
							"lookup",                // Default subcommand without arguments
							null,                    // Default subcommand with arguments
							new String[] { "status" }),  // Array of provided subcommands 
			});
	// @formatter:on

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public MessageContent handleCommand(Command command) {
		MessageContent msg;
		if (command.getSubCommand().equals("lookup")) {
			msg = new MessageContent("TODO contact lookup");
		}
		else {
			msg = new MessageContent("Unkown command");
		}
		return msg;
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context).getLogSettings());
	}
}
