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

import org.projectmaxs.main.MAXSService;
import org.projectmaxs.shared.global.StatusInformation;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.MAXSStatusUtil;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

public class MAXSAmbientTemperatureManager extends MAXSService.StartStopListener
		implements SensorEventListener {

	private static final Log LOG = Log.getLog();

	private static MAXSAmbientTemperatureManager sMaxsAmbientTemperatureManager;

	public synchronized static void init(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return;
		}
		if (sMaxsAmbientTemperatureManager == null) {
			sMaxsAmbientTemperatureManager = new MAXSAmbientTemperatureManager(context);
		}
	}

	private final Context mContext;
	private final MAXSBatteryManager mMaxsBatteryManager;
	private final SensorManager mSensorManager;
	private final Sensor mAmbientTemperatureSensor;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private MAXSAmbientTemperatureManager(Context context) {
		mContext = context;
		mMaxsBatteryManager = MAXSBatteryManager.getInstance(context);
		MAXSService.addStartStopListener(this);

		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		mAmbientTemperatureSensor = mSensorManager
				.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		if (mAmbientTemperatureSensor != null) {
			LOG.i("Found ambient temperature sensor: " + mAmbientTemperatureSensor);
		} else {
			LOG.i("No ambient temperature sensor found :(");
		}
	}

	@Override
	public void onServiceStart(MAXSService service) {
		if (mAmbientTemperatureSensor == null) {
			return;
		}
		mSensorManager.registerListener(this, mAmbientTemperatureSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	public void onServiceStop(MAXSService service) {
		if (mAmbientTemperatureSensor == null) {
			return;
		}
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float ambientTemperature = event.values[0];

		String ambientTemperaturePotentiallyScaled = mMaxsBatteryManager
				.maybeFloatToRangeUnscaled(ambientTemperature, 2);

		StatusInformation statusInformation = new StatusInformation("ambient-temperature",
				ambientTemperaturePotentiallyScaled + "°C", Float.toString(ambientTemperature));
		MAXSStatusUtil.maybeUpdateStatus(mContext, statusInformation);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		LOG.d("Ambient temperature accuracy changed to " + accuracy);
	}
}
