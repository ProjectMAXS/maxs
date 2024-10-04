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

package org.projectmaxs.module.locationfine.service;

import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.module.locationfine.LocationUtil;
import org.projectmaxs.module.locationfine.ModuleService;
import org.projectmaxs.module.locationfine.service.gpsenabler.GpsEnablerDisabler;
import org.projectmaxs.module.locationfine.service.gpsenabler.PowerWidgetFlaw;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.ILocationFineModuleLocationService;
import org.projectmaxs.shared.module.MainUtil;
import org.projectmaxs.shared.module.SharedLocationUtil;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service {

	public static final String START_SERVICE_NOT_STICKY = ModuleService.LOCATIONFINE_MODULE_PACKAGE
			+ ".LOCATION_START_SERVICE_NOT_STICKY";
	public static final String START_SERVICE = ModuleService.LOCATIONFINE_MODULE_PACKAGE
			+ ".LOCATION_START_SERVICE";
	public static final String STOP_SERVICE = ModuleService.LOCATIONFINE_MODULE_PACKAGE
			+ ".LOCATION_STOP_SERVICE";

	/**
	 * Interval in milliseconds after which a new location is always considered better.
	 * 
	 * Currently 2 minutes
	 */
	private static final int UPDATE_INTERVAL = 1000 * 60 * 2;

	private static final Log LOG = Log.getLog();

	private static final List<GpsEnablerDisabler> GPS_ENABLER_DISABLERS = new LinkedList<GpsEnablerDisabler>();

	static {
		GPS_ENABLER_DISABLERS.add(new PowerWidgetFlaw());
	}

	private class LocationServiceLocationListener implements LocationListener {

		@Override
		public final void onLocationChanged(Location location) {
			LOG.d("onLocationChanged: locaction=" + location);

			String betterLocationStatus;
			if (isBetterLocation(location)) {
				mCurrentBestLocation = location;
				onBetterLocation(location);
				betterLocationStatus = "was";
			} else {
				betterLocationStatus = "was not";
			}
			LOG.d("onLocationChanged: " + location + ' ' + betterLocationStatus
					+ " a better location then the current best location " + mCurrentBestLocation);
		}

		protected void onBetterLocation(Location location) {}

		@Override
		public final void onStatusChanged(String provider, int status, Bundle extras) {}

		@Override
		public final void onProviderEnabled(String provider) {}

		@Override
		public final void onProviderDisabled(String provider) {}

	}

	private LocationListener mLocationListener = new LocationServiceLocationListener() {
		@Override
		protected void onBetterLocation(Location location) {
			send(location);
		}
	};

	private LocationManager mLocationManager;
	private Location mCurrentBestLocation;

	private List<String> mAllProviders;

	private Command mCommand;

	private boolean mGpsManuallyEnabled = false;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final ILocationFineModuleLocationService.Stub mBinder = new ILocationFineModuleLocationService.Stub() {

		@Override
		public Location getCurrentBestLocation() {
			List<Location> lastKnownLocations = LocationUtil
					.getLastKnownLocationsSorted(mLocationManager);
			if (lastKnownLocations.isEmpty()) {
				return null;
			}
			return lastKnownLocations.get(0);
		}

		@Override
		public List<Location> getCurrentBestLocations() {
			List<Location> lastKnownLocations = LocationUtil
					.getLastKnownLocationsSorted(mLocationManager);
			return lastKnownLocations;
		}

	};

	@Override
	public void onCreate() {
		super.onCreate();

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mAllProviders = mLocationManager.getAllProviders();
	}

	// TODO module-locationfine uses a 'dangerous' permission, which requires as of Android 6.0 (23)
	// or higher a the user's grant on runtime (additional to the permission on install time).
	// Implement this.
	@SuppressLint("MissingPermission")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) intent = new Intent(START_SERVICE);

		Command command = intent.getParcelableExtra(GlobalConstants.EXTRA_COMMAND);
		if (command != null) {
			mCommand = command;
		}

		final String action = intent.getAction();
		LOG.d("onStartCommand: action=" + action + ", flags=" + flags + ", startId=" + startId);

		boolean startSticky = false;
		switch (action) {
		case START_SERVICE_NOT_STICKY:
			for (String provider : mAllProviders) {
				mLocationManager.requestSingleUpdate(provider,
						new LocationServiceLocationListener(), null);
			}
			break;
		case START_SERVICE:
			if (!tryEnableGps()) send(new Message("GPS was disabled and we could not enable it."));

			for (String provider : mAllProviders)
				mLocationManager.requestLocationUpdates(provider, 1000 * 30, 5, mLocationListener);
			for (String provider : mAllProviders) {
				Location location = mLocationManager.getLastKnownLocation(provider);
				if (location != null && isBetterLocation(location)) {
					send(location);
					break;
				}
			}
			startSticky = true;
			break;
		case STOP_SERVICE:
			mLocationManager.removeUpdates(mLocationListener);
			if (mGpsManuallyEnabled) {
				// Only reset to false if disabling GPS was successful
				if (tryDisableGps()) mGpsManuallyEnabled = false;
			}
			stopSelfResult(startId);
			break;
		default:
			throw new IllegalStateException("Unknown action: " + action);
		}

		if (Build.VERSION.SDK_INT >= 31) {
			// Starting apps targeting API level 31 or higher are
			// not allowed to start a sticky foreground service
			// from background.
			startSticky = false;
		}
		return startSticky ? START_STICKY : START_NOT_STICKY;
	}

	private boolean gpsEnabled() {
		return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	private boolean tryEnableGps() {
		if (gpsEnabled()) return true;

		for (GpsEnablerDisabler enabler : GPS_ENABLER_DISABLERS) {
			try {
				enabler.enableGps(this);
			} catch (Throwable e) {
				LOG.d("tryEnableGps", e);
			}
			if (gpsEnabled()) {
				mGpsManuallyEnabled = true;
				return true;
			}
		}
		return false;
	}

	private boolean tryDisableGps() {
		if (!gpsEnabled()) return true;

		for (GpsEnablerDisabler enabler : GPS_ENABLER_DISABLERS) {
			try {
				enabler.disableGps(this);
			} catch (Throwable e) {
				LOG.d("tryDisableGps", e);
			}
			if (!gpsEnabled()) return true;
		}
		return false;
	}

	private void send(Location location) {
		Message message = SharedLocationUtil.toMessage(location);
		send(message);
	}

	private void send(Message message) {
		if (mCommand != null) {
			message.setId(mCommand.getId());
		}
		MainUtil.send(message, this);
	}

	@TargetApi(17)
	private boolean isBetterLocation(Location newLocation) {
		if (mCurrentBestLocation == null) return true;

		long timeDelta;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			timeDelta = newLocation.getElapsedRealtimeNanos()
					- mCurrentBestLocation.getElapsedRealtimeNanos();
		} else {
			timeDelta = newLocation.getTime() - mCurrentBestLocation.getTime();
		}
		if (timeDelta < 0) {
			// The new location is older then the current best location, therefore it's not a better
			// location
			LOG.w("new location older then current best location");
			return false;
		}

		float accuracyDelta = newLocation.getAccuracy() - mCurrentBestLocation.getAccuracy();
		boolean newLocationIsMoreAccurate = accuracyDelta < 0;
		if (newLocationIsMoreAccurate) return true;

		// We consider two locations to have the same accuracy if the differ only within 5 meters
		boolean newLocationIsSignificantlyNewer = timeDelta > UPDATE_INTERVAL;
		boolean newLocationHasSameAccurary = Math.abs(accuracyDelta) < 5;
		boolean newLocationIsSameAsCurrent = newLocationHasSameAccurary
				&& newLocation.getAltitude() == mCurrentBestLocation.getAltitude()
				&& newLocation.getLongitude() == mCurrentBestLocation.getLongitude();
		if (!newLocationIsSameAsCurrent
				&& (newLocationIsSignificantlyNewer || newLocationHasSameAccurary)) return true;

		return false;
	}

}
