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

package org.projectmaxs.module.clipboard;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.UnkownCommandException;
import org.projectmaxs.shared.module.UnkownSubcommandException;

import android.content.Context;
import android.text.ClipboardManager;

@SuppressWarnings("deprecation")
public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	private ClipboardManager mManager;

	public ModuleService() {
		super(LOG, "maxs-module-clipboard");
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.clipboard",      // Package of the Module
			"clipboard",                             // Name of the Module (if omitted, last substring after '.' is used)
			new ModuleInformation.Command[] {        // Array of commands provided by the module
					new ModuleInformation.Command(
							"clipboard",             // Command name
							"clip",                    // Short command name
							"get",                // Default subcommand without arguments
							null,                    // Default subcommand with arguments
							new String[] { "set", "get" }),  // Array of provided subcommands 
			});
	// @formatter:on

	@Override
	public void onCreate() {
		super.onCreate();
		mManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
	}

	@Override
	public Message handleCommand(Command command) {
		final String cmd = command.getCommand();
		final String subCmd = command.getSubCommand();

		final Message msg;
		if ("clipboard".equals(cmd) || "clip".equals(cmd)) {
			if ("get".equals(subCmd)) {
				msg = new Message("Clipboard: " + mManager.getText());
			} else if ("set".equals(subCmd)) {
				mManager.setText(command.getArgs());
				msg = new Message("Clipboard set to: " + command.getArgs());
			} else {
				throw new UnkownSubcommandException(command);
			}
		} else {
			throw new UnkownCommandException(command);
		}
		return msg;
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}
}
