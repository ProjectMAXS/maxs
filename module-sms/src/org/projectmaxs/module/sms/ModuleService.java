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

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;

import android.content.Context;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-sms");
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
		Message message;
		if (command.getSubCommand().equals("read")) {
			message = new Message("Hello from sms module");
		}
		else {
			message = new Message("Unkown command");
		}
		return message;
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context).getLogSettings());
	}

}
