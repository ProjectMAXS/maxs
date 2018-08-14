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

package org.projectmaxs.main.receivers;

import org.projectmaxs.main.MAXSService;
import org.projectmaxs.main.Settings;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.shared.global.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

	private static final Log LOG = Log.getLog();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action == null || !action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			LOG.w("Received invalid, possibly spoofed, intent: " + intent);
			return;
		}

		if (Settings.getInstance(context).connectOnBootCompleted()) {
			Intent startServiceIntent = new Intent(Constants.ACTION_START_SERVICE);
			startServiceIntent.setClass(context, MAXSService.class);
			context.startService(startServiceIntent);
		}
	}

}
