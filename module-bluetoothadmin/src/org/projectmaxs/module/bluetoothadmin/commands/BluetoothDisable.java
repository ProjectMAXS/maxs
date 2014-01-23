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

package org.projectmaxs.module.bluetoothadmin.commands;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;

public class BluetoothDisable extends AbstractBluetoothCommand {

	public BluetoothDisable() {
		super("disable");
		setHelp(ArgType.NONE, "Disable the bluetooth adapter");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		Message message = checkDefaultAdapter();
		if (message != null) return message;

		boolean res = getDefaultAdapter().disable();
		if (res) {
			message = new Message("Disabling bluetooth adapter");
			registerBluetoothReceiver(command.getId(), service);
		} else {
			message = new Message("Failed to disable bluetooth adapter");
		}
		return message;
	}

}
