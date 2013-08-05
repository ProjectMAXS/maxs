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

package org.projectmaxs.module.battery;

import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.shared.StatusInformation;

import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class MAXSBatteryManager {
	private static final int STEP = 5;
	private static final String AC = "AC";
	private static final String USB = "USB";
	private static final String BAT = "Battery";

	private static MAXSBatteryManager sBatteryManager;

	public synchronized static MAXSBatteryManager getInstance(Context context) {
		if (sBatteryManager == null) sBatteryManager = new MAXSBatteryManager(context);
		return sBatteryManager;
	}

	private final Context mContext;
	private final Settings mSettings;

	private MAXSBatteryManager(Context context) {
		this.mContext = context;
		this.mSettings = Settings.getInstance(context);
	}

	public List<StatusInformation> onBatteryChangedReceived(Intent intent) {
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
				|| status == BatteryManager.BATTERY_STATUS_FULL;

		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		String powerSource = BAT;
		switch (plugged) {
		case BatteryManager.BATTERY_PLUGGED_AC:
			powerSource = AC;
			break;
		case BatteryManager.BATTERY_PLUGGED_USB:
			powerSource = USB;
			break;
		}

		int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
		int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
		int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		float batteryPct = level / (float) scale;

		float lastBatteryPct = mSettings.getBatteryPercentage();
		int lastPlugged = mSettings.getBatteryPlugStatus();

		String batteryPctStr = maybeIntToRange((int) batteryPct, isCharging);
		String lastBatteryPctStr = maybeIntToRange((int) lastBatteryPct, isCharging);

		if (plugged == lastPlugged && batteryPctStr.equals(lastBatteryPctStr)) return null;

		List<StatusInformation> res = new LinkedList<StatusInformation>();
		if (plugged != lastPlugged) res.add(new StatusInformation("BAT_PLUGGED", powerSource));
		if (!batteryPctStr.equals(lastBatteryPctStr)) res.add(new StatusInformation("BAT_PCT", batteryPctStr + '%'));
		return res;
	}

	private static String maybeIntToRange(int in, boolean isCharging) {
		if (isCharging) return Integer.toString(in);

		int lowerBound = (in / 5) * STEP;
		if (lowerBound != 100) {
			int upperBound = lowerBound + STEP;
			return lowerBound + "-" + upperBound;
		}
		else {
			return Integer.toString(lowerBound);
		}
	}
}
