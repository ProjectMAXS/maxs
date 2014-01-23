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

package org.projectmaxs.module.bluetooth.commands;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.ModuleConstants;
import org.projectmaxs.shared.module.SubCommand;
import org.projectmaxs.shared.module.messagecontent.BooleanElement;

import android.bluetooth.BluetoothAdapter;

public class BluetoothStatus extends SubCommand {

	public BluetoothStatus() {
		super(ModuleConstants.BLUEOOTH, "status", true);
		setHelp(ArgType.NONE, "Show the current status of the bluetooth adapter");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service) {
		final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null)
			return new Message("BT Adapter is null. Maybe this device does not support bluetooth?");

		return new Message(BooleanElement.enabled("Bluetooth is %1$s", "bluetooth_adapter_enabled",
				adapter.isEnabled(), service));
	}
}
