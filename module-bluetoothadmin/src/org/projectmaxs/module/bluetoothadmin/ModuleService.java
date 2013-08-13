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

package org.projectmaxs.module.bluetoothadmin;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	private BluetoothAdapter mAdapter;

	public ModuleService() {
		super(LOG, "maxs-module-bluetoothadmin");
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.bluetoothadmin",      // Package of the Module
			"bluetoothadmin",                             // Name of the Module (if omitted, last substring after '.' is used)
			new ModuleInformation.Command[] {        // Array of commands provided by the module
					new ModuleInformation.Command(
							"bluetooth",             // Command name
							"bt",                    // Short command name
							null,                // Default subcommand without arguments
							null,                    // Default subcommand with arguments
							new String[] { "enable", "disable" }),  // Array of provided subcommands 
			});
	// @formatter:on

	@Override
	public void onCreate() {
		super.onCreate();
		mAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	@Override
	public Message handleCommand(Command command) {
		if (mAdapter == null) return new Message("BT Adapter is null. Maybe this device does not support bluetooth?");

		Message msg;
		final String subCommand = command.getSubCommand();
		final int commandId = command.getId();
		if ("enable".equals(subCommand)) {
			boolean res = mAdapter.enable();
			if (res) {
				msg = new Message("Enabling bluetooth adapter");
				registerBluetoothReceiver(commandId);
			}
			else {
				msg = new Message("Failed to enable bluetooth adapter");
			}
		}
		else if ("disable".equals(subCommand)) {
			boolean res = mAdapter.disable();
			if (res) {
				msg = new Message("Disabling bluetooth adapter");
				registerBluetoothReceiver(commandId);
			}
			else {
				msg = new Message("Failed to disable bluetooth adapter");
			}
		}
		else {
			msg = new Message("Unkown command");
		}
		return msg;
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}

	private static String stateToString(int state) {
		String res;
		switch (state) {
		case BluetoothAdapter.STATE_OFF:
			res = "off";
			break;
		case BluetoothAdapter.STATE_ON:
			res = "on";
			break;
		case BluetoothAdapter.STATE_TURNING_OFF:
			res = "turning off";
			break;
		case BluetoothAdapter.STATE_TURNING_ON:
			res = "turning on";
			break;
		default:
			throw new IllegalStateException();
		}
		return res;
	}

	private void registerBluetoothReceiver(int commandId) {
		final IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(new BluetoothStateReceiver(commandId), filter);
	}

	private class BluetoothStateReceiver extends BroadcastReceiver {
		private final int mCommandId;

		private BluetoothStateReceiver(int commandId) {
			addPendingAction(this);
			mCommandId = commandId;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			int stateInt = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
			String state = stateToString(stateInt);
			String prevState = stateToString(intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1));
			LOG.d("Bluetooth adapter changed state from '" + prevState + "' to '" + state + "'");
			Message msgContent = new Message("Bluetooth adapter changed state from '" + prevState + "' to '" + state
					+ "'");
			sendMessage(msgContent, mCommandId);

			// unregister this receiver if we have reached an end state
			if (stateInt == BluetoothAdapter.STATE_OFF || stateInt == BluetoothAdapter.STATE_ON) {
				unregisterReceiver(this);
				removePendingAction(this);
			}
		}
	}
}
