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
import org.projectmaxs.shared.global.StatusInformation;
import org.projectmaxs.shared.global.util.Unicode;
import org.projectmaxs.shared.mainmodule.MAXSStatusUtil;

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

	public static void init(Context context) {
		getInstance(context);
	}

	public synchronized static MAXSBatteryManager getInstance(Context context) {
		if (sBatteryManager == null) {
			sBatteryManager = new MAXSBatteryManager(context);
		}
		return sBatteryManager;
	}

	private final Context mContext;
	private final BroadcastReceiver mBatteryBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			onBatteryChangedReceived(intent);
		}
	};

	private volatile boolean mIsCharging;

	private String mLastBatteryPct = "";
	private int mLastPlugged = -1;
	private int mLastHealth = -1;
	private String mLastVoltage = "";
	private String mLastTemperature = "";

	private MAXSBatteryManager(Context context) {
		mContext = context;
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
		switch (status) {
		case BatteryManager.BATTERY_STATUS_FULL:
		case BatteryManager.BATTERY_STATUS_CHARGING:
			mIsCharging = true;
			break;
		default:
			mIsCharging = false;
			break;
		}

		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

		int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
		int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
		float temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10;
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		// batteryPctFloat is the battery charge status between 0.0f and 1.0f.
		float batteryPctFloat = level / (float) scale;
		String batteryPct = maybeFloatToRange(batteryPctFloat);

		ArrayList<StatusInformation> infos = new ArrayList<>(5);
		if (plugged != mLastPlugged) {
			infos.add(new StatusInformation("power-source", getPowerSource(status, plugged)));
			mLastPlugged = plugged;
		}
		if (!batteryPct.equals(mLastBatteryPct)) {
			infos.add(new StatusInformation("battery-percentage", batteryPct + '%',
					Float.toString(batteryPctFloat)));
			mLastBatteryPct = batteryPct;
		}
		if (health != mLastHealth) {
			String healthString = healthToString(health);
			infos.add(new StatusInformation("battery-health", null, healthString));
			mLastHealth = health;
		}

		String batteryVoltageString = maybeFloatToRangeUnscaled(voltage, 1);
		if (!mLastVoltage.equals(batteryVoltageString)) {
			infos.add(new StatusInformation("battery-voltage", null, Integer.toString(voltage)));
			mLastVoltage = batteryVoltageString;
		}

		String batteryTemperatureString = maybeFloatToRangeUnscaled(temperature, 2);
		if (!mLastTemperature.equals(batteryTemperatureString)) {
			infos.add(new StatusInformation("battery-temperature",
					Unicode.BATTERY + ' ' + batteryTemperatureString + "°C",
					Float.toString(temperature)));
			mLastTemperature = batteryVoltageString;
		}

		MAXSStatusUtil.maybeUpdateStatus(mContext, infos);
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
		case 4: // BatteryManager.BATTERY_PLUGGED_WIRELESS is API 17
			powerSource = "WirelessPower";
			break;
		case 0:
			powerSource = BAT;
			break;
		default:
			powerSource = "Unknown (" + plugged + ')';
			break;
		}
		return powerSource;
	}

	private static String healthToString(int health) {
		String healthString;
		switch (health) {
		case BatteryManager.BATTERY_HEALTH_UNKNOWN:
			healthString = "unknown";
			break;
		case BatteryManager.BATTERY_HEALTH_GOOD:
			healthString = "good";
			break;
		case BatteryManager.BATTERY_HEALTH_OVERHEAT:
			healthString = "overheat";
			break;
		case BatteryManager.BATTERY_HEALTH_DEAD:
			healthString = "dead";
			break;
		case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
			healthString = "over voltage";
			break;
		case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
			healthString = "unspecified failure";
			break;
		case BatteryManager.BATTERY_HEALTH_COLD:
			healthString = "cold";
			break;
		default:
			healthString = "unknown (" + health + ')';
			break;
		}
		return healthString;
	}

	public String maybeFloatToRangeUnscaled(float f, int step) {
		if (mIsCharging) return Float.toString(f);

		int lowerBound = ((int) f / step) * step;
		int upperBound = lowerBound + step;
		return lowerBound + "-" + upperBound;
	}

	private String maybeFloatToRange(float f) {
		int in = (int) (f * 100);
		if (mIsCharging) return Integer.toString(in);

		int lowerBound = (in / 5) * STEP;
		if (lowerBound != 100) {
			int upperBound = lowerBound + STEP;
			return lowerBound + "-" + upperBound;
		} else {
			return Integer.toString(lowerBound);
		}
	}
}
