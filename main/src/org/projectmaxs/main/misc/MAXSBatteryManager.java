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

package org.projectmaxs.main.misc;

import java.util.ArrayList;

import org.projectmaxs.main.MAXSService;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.mainmodule.StatusInformation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class MAXSBatteryManager extends MAXSService.StartStopListener {
	private static final int STEP = 5;
	private static final String AC = "AC";
	private static final String USB = "USB";
	private static final String BAT = "Battery";

	private static MAXSBatteryManager sBatteryManager;

	public synchronized static void init(Context context) {
		if (sBatteryManager == null) sBatteryManager = new MAXSBatteryManager(context);
	}

	private final Context mContext;
	private final BroadcastReceiver mBatteryBroadcastReceiver;

	private String mLastBatteryPct = "";
	private int mLastPlugged = -1;

	private MAXSBatteryManager(Context context) {
		mContext = context;
		mBatteryBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				onBatteryChangedReceived(intent);
			};
		};
		MAXSService.addStartStopListener(this);
	}

	@Override
	public void onServiceStart(MAXSService service) {
		mContext.registerReceiver(mBatteryBroadcastReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	public void onServiceStop(MAXSService service) {
		mContext.unregisterReceiver(mBatteryBroadcastReceiver);
	}

	private void onBatteryChangedReceived(Intent intent) {
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = isCharging(status);

		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

		// int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
		// int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
		// int temperature =
		// intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		String batteryPct = maybeFloatToRange(level / (float) scale, isCharging);

		ArrayList<StatusInformation> infos = new ArrayList<StatusInformation>(2);
		if (plugged != mLastPlugged)
			infos.add(new StatusInformation("BAT_PLUGGED", getPowerSource(status, plugged)));
		if (!batteryPct.equals(mLastBatteryPct))
			infos.add(new StatusInformation("BAT_PCT", batteryPct + '%'));
		// Be done here if there are now new status information to report
		if (infos.size() == 0) return;

		mLastBatteryPct = batteryPct;
		mLastPlugged = plugged;

		Intent replyIntent = new Intent(GlobalConstants.ACTION_UPDATE_STATUS);
		replyIntent.putParcelableArrayListExtra(GlobalConstants.EXTRA_CONTENT, infos);
		mContext.startService(replyIntent);
	}

	private static boolean isCharging(int status) {
		return status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;
	}

	private static String getPowerSource(int status, int plugged) {
		String powerSource;
		switch (plugged) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			powerSource = AC;
			break;
		case BatteryManager.BATTERY_PLUGGED_USB:
			powerSource = USB;
			break;
		case 0:
			powerSource = BAT;
			break;
		default:
			powerSource = "Unkown";
			break;
		}
		return powerSource;
	}

	private static String maybeFloatToRange(float f, boolean isCharging) {
		int in = (int) (f * 100);
		if (isCharging) return Integer.toString(in);

		int lowerBound = (in / 5) * STEP;
		if (lowerBound != 100) {
			int upperBound = lowerBound + STEP;
			return lowerBound + "-" + upperBound;
		} else {
			return Integer.toString(lowerBound);
		}
	}
}
