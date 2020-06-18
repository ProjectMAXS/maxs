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
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.Unicode;
import org.projectmaxs.shared.mainmodule.MAXSStatusUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class MAXSBatteryManager extends MAXSService.StartStopListener {
	private static final Log LOG = Log.getLog();

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

	private volatile boolean mPowerConsumptionDoesNotMatter;

	private RangedNumber<Float> mLastBattery;
	private int mLastPlugged = -1;
	private int mLastHealth = -1;
	private RangedNumber<Integer> mLastVoltage;
	private RangedNumber<Float> mLastTemperature;

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
			mPowerConsumptionDoesNotMatter = true;
			break;
		default:
			mPowerConsumptionDoesNotMatter = false;
			break;
		}

		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

		int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
		int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
		float temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10;
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		ArrayList<StatusInformation> infos = new ArrayList<>(5);

		if (plugged != mLastPlugged) {
			infos.add(new StatusInformation("power-source", getPowerSource(status, plugged)));
			mLastPlugged = plugged;
		}

		// batteryPctFloat is the battery charge status between 0.0f and 1.0f.
		float batteryPctFloat = level / (float) scale;
		// This is the battery charge status between 0 and 100.
		float batteryScaledFloat = batteryPctFloat * 100;
		if (mLastBattery == null || mLastBattery.doesNotRepresentNumber(batteryScaledFloat)) {
			mLastBattery = new RangedNumber<Float>(batteryScaledFloat, 5);
			infos.add(new StatusInformation("battery-percentage", mLastBattery.toDynamicString() + '%',
					mLastBattery.getConcreteValue()));
		}

		if (health != mLastHealth) {
			String healthString = healthToString(health);
			infos.add(new StatusInformation("battery-health", null, healthString));
			mLastHealth = health;
		}

		if (mLastVoltage == null || mLastVoltage.doesNotRepresentNumber(voltage)) {
			LOG.d("Last battery voltage " + mLastVoltage + " does not represent current voltage " + voltage);
			mLastVoltage = new RangedNumber<Integer>(voltage, 500);
			infos.add(new StatusInformation("battery-voltage", null, mLastVoltage.getConcreteValue()));
		}

		if (mLastTemperature == null || mLastTemperature.doesNotRepresentNumber(temperature)) {
			mLastTemperature = new RangedNumber<Float>(temperature, 2);
			infos.add(new StatusInformation("battery-temperature",
					Unicode.BATTERY + ' ' + mLastTemperature.toDynamicString() + "°C",
					mLastTemperature.getConcreteValue()));
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

	public <N extends Number> RangedNumber<N> createRangedNumber(N n, int step) {
		return new RangedNumber<N>(n, step);
	}

	public class RangedNumber<N extends Number> {
		private final N n;
		private final int lowerBound;
		private final int upperBound;

		private RangedNumber(N n, int step) {
			this.n = n;

			// i is n's integer representation.
			int i = Math.round(n.floatValue());
			int stepHalf = step / 2;

			this.lowerBound = i - stepHalf;
			this.upperBound = i + stepHalf;
		}

		public boolean doesNotRepresentNumber(N n) {
			return !doesRepresentNumber(n);
		}

		public boolean doesRepresentNumber(N n) {
			if (mPowerConsumptionDoesNotMatter) {
				return this.n.equals(n);
			}
			int nAsInt = n.intValue();
			return lowerBound >= nAsInt && nAsInt <= upperBound;
		}

		public String getConcreteValue() {
			return n.toString();
		}

		@Override
		public String toString() {
			final boolean powerConsumptionDoesNotMatter = mPowerConsumptionDoesNotMatter;
			StringBuilder sb = new StringBuilder();
			sb.append('[').append(lowerBound).append(' ');
			if (powerConsumptionDoesNotMatter) {
				sb.append('(');
			}
			sb.append(n);
			if (powerConsumptionDoesNotMatter) {
				sb.append(')');
			}
			sb.append(' ').append(upperBound).append(']');
			return sb.toString();
		}

		public String toDynamicString() {
			if (mPowerConsumptionDoesNotMatter) {
				return String.valueOf(n);
			}
			return Integer.toString(lowerBound) + '-' + Integer.toString(upperBound);
		}

	}
}
