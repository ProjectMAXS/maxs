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

package org.projectmaxs.module.sms;

import org.projectmaxs.shared.Command;
import org.projectmaxs.shared.Message;
import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.sharedmodule.MAXSModuleIntentService;

public class ModuleService extends MAXSModuleIntentService {

	public ModuleService() {
		super("MAXSModule:sms");
	}

	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation("org.projectmaxs.module.sms",
			new ModuleInformation.Command[] { new ModuleInformation.Command("sms", "s", "read", "read",
					new String[] { "read" }), });

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public Message handleCommand(Command command) {
		Message msg;
		if (command.getSubCommand().equals("read")) {
			msg = new Message("Hello from sms module");
		}
		else {
			msg = new Message("Unkown command");
		}
		return msg;
	}

}
