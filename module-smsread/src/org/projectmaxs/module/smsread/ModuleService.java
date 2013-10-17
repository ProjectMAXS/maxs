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

package org.projectmaxs.module.smsread;

import java.util.List;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Sms;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.UnkownCommandException;
import org.projectmaxs.shared.module.UnkownSubcommandException;

import android.content.Context;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-smsread");
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.smsread",      // Package of the Module
			"smsread",                             // Name of the Module (if omitted, last substring after '.' is used)
			new ModuleInformation.Command[] {        // Array of commands provided by the module
					new ModuleInformation.Command(
							"sms",             // Command name
							"s",                    // Short command name
							"show",                // Default subcommand without arguments
							null,                    // Default subcommand with arguments
							new String[] { "show" }),  // Array of provided subcommands
			});
	// @formatter:on

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public Message handleCommand(Command command) {
		final String cmd = command.getCommand();
		final String subCmd = command.getSubCommand();

		Message msg = null;
		if ("sms".equals(cmd) || "s".equals(cmd)) {
			if ("show".equals(subCmd)) {
				msg = show(command.getArgs());
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

	private final Message show(String args) {
		if (args != null && !SharedStringUtil.isInteger(args)) {
			// TODO show last messages by string (e.g. contact name or number)
			return new Message("not implemented yet");
		}

		int count;
		if (args == null) {
			count = 5;
		} else {
			count = Integer.parseInt(args);
		}
		Message msg = new Message();
		Text text = Text.get().addBoldNL("Last " + count + " SMS messages");
		List<Sms> sms = SmsUtil.getOrderedSMS(null, null, count, this);
		msg.add(text).addAll(sms);

		return msg;
	}
}
