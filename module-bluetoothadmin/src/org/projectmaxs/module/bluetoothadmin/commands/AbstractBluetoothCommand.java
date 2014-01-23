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
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.ModuleConstants;
import org.projectmaxs.shared.module.SubCommand;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class AbstractBluetoothCommand extends SubCommand {

	private static final Log LOG = Log.getLog();

	private static final BluetoothAdapter sADAPTER = BluetoothAdapter.getDefaultAdapter();

	public AbstractBluetoothCommand(String name) {
		super(ModuleConstants.BLUEOOTH, name);
	}

	BluetoothAdapter getDefaultAdapter() {
		return sADAPTER;
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

	Message checkDefaultAdapter() {
		if (getDefaultAdapter() == null) {
			return new Message("BT Adapter is null. Maybe this device does not support bluetooth?");
		} else {
			return null;
		}
	}

	void registerBluetoothReceiver(int commandId, MAXSModuleIntentService service) {
		final IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		service.registerReceiver(new BluetoothStateReceiver(commandId, service), filter);
	}

	private class BluetoothStateReceiver extends BroadcastReceiver {
		private final int mCommandId;
		private final MAXSModuleIntentService mService;

		private BluetoothStateReceiver(int commandId, MAXSModuleIntentService service) {
			service.addPendingAction(this);
			mCommandId = commandId;
			mService = service;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			int stateInt = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
			String state = stateToString(stateInt);
			String prevState = stateToString(intent.getIntExtra(
					BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1));
			LOG.d("Bluetooth adapter changed state from '" + prevState + "' to '" + state + "'");
			Message msgContent = new Message("Bluetooth adapter changed state from '" + prevState
					+ "' to '" + state + "'");
			mService.send(msgContent, mCommandId);

			// unregister this receiver if we have reached an end state
			if (stateInt == BluetoothAdapter.STATE_OFF || stateInt == BluetoothAdapter.STATE_ON) {
				mService.unregisterReceiver(this);
				mService.removePendingAction(this);
			}
		}
	}
}
