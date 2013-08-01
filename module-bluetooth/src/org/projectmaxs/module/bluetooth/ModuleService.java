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

package org.projectmaxs.module.bluetooth;

import org.projectmaxs.shared.Command;
import org.projectmaxs.shared.MessageContent;
import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.sharedmodule.MAXSModuleIntentService;

import android.bluetooth.BluetoothAdapter;

public class ModuleService extends MAXSModuleIntentService {

	private BluetoothAdapter mAdapter;

	public ModuleService() {
		super("maxs-module-bluetooth");
	}

	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.bluetooth", new ModuleInformation.Command[] { new ModuleInformation.Command(
					"bluetooth", "bt", "status", null, new String[] { "status" }), });

	@Override
	public void onCreate() {
		super.onCreate();
		mAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public MessageContent handleCommand(Command command) {
		if (mAdapter == null)
			return new MessageContent("BT Adapter is null. Maybe this devices does not support bluetooth?");

		MessageContent msg;
		if (command.getSubCommand().equals("status")) {
			msg = new MessageContent("Bluetooth is enabled: " + mAdapter.isEnabled());
		}
		else {
			msg = new MessageContent("Unkown command");
		}
		return msg;
	}
}
