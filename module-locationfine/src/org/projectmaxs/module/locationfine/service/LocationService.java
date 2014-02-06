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

import org.projectmaxs.module.locationfine.ModuleService;
import org.projectmaxs.module.locationfine.service.gpsenabler.GpsEnablerDisabler;
import org.projectmaxs.module.locationfine.service.gpsenabler.PowerWidgetFlaw;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.global.util.DateTimeUtil;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MainUtil;

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

	private LocationListener mLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			LOG.d("onLocationChanged: locaction=" + location);
			if (isBetterLocation(location)) send(location);
		}

		@Override
		public void onProviderDisabled(String provider) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}

	};

	private LocationManager mLocationManager;
	private Location mCurrentBestLocation;

	private List<String> mAllProviders;

	private Command mCommand = new Command();

	private boolean mGpsManuallyEnabled = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mAllProviders = mLocationManager.getAllProviders();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) intent = new Intent(START_SERVICE);

		mCommand = intent.getParcelableExtra(GlobalConstants.EXTRA_COMMAND);
		final String action = intent.getAction();
		LOG.d("onStartCommand: action=" + action + ", flags=" + flags + ", startId=" + startId);

		if (START_SERVICE.equals(action)) {
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
		} else if (STOP_SERVICE.equals(action)) {
			mLocationManager.removeUpdates(mLocationListener);
			if (mGpsManuallyEnabled) {
				// Only reset to false if disabling GPS was successful
				if (tryDisableGps()) mGpsManuallyEnabled = false;
			}
			stopSelfResult(startId);
		} else {
			throw new IllegalStateException("Unkown action: " + action);
		}
		return START_STICKY;
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
		mCurrentBestLocation = location;

		String latitude = Double.toString(location.getLatitude());
		String longitude = Double.toString(location.getLongitude());
		String time = Long.toString(location.getTime());
		String humanTime = DateTimeUtil.shortFromUtc(location.getTime());

		String accuracy = location.hasAccuracy() ? Float.toString(location.getAccuracy()) : null;
		String altitude = location.hasAltitude() ? Double.toString(location.getAltitude()) : null;
		String speed = location.hasSpeed() ? Float.toString(location.getSpeed()) : null;

		Message message = new Message();

		Text text = new Text();
		text.addBoldNL("Location (" + humanTime + ')');
		text.addNL("Latitude: " + latitude + " Longitude: " + longitude);
		text.addNL("http://www.openstreetmap.org/?mlat=" + latitude + "&mlon=" + longitude
				+ "&zoom=14&layers=M");
		if (accuracy != null) text.addNL("Accuracy: " + accuracy);
		if (altitude != null) text.addNL("Altitude: " + altitude);
		if (speed != null) text.addNL("Speed: " + speed);
		message.add(text);

		// Add a non human-readable element with that information
		Element element = new Element("location");
		element.addChildElement(new Element("latitude", latitude));
		element.addChildElement(new Element("longitude", longitude));
		element.addChildElement(new Element("time", time));
		if (accuracy != null) element.addChildElement(new Element("accuracy", accuracy));
		if (altitude != null) element.addChildElement(new Element("altitude", altitude));
		if (speed != null) element.addChildElement(new Element("speed", speed));
		message.add(element);

		send(message);
	}

	private void send(Message message) {
		message.setId(mCommand.getId());
		MainUtil.send(message, this);

	}

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

		boolean newLocationIsSignificantlyNewer = timeDelta > UPDATE_INTERVAL;
		float accuracyDelta = newLocation.getAccuracy() - mCurrentBestLocation.getAccuracy();
		boolean newLocationIsMoreAccurate = accuracyDelta < 0;
		if (newLocationIsMoreAccurate) return true;

		// We consider two locations to have the same accuracy if the differ only within 5 meters
		boolean newLocationHasSameAccurary = Math.abs(accuracyDelta) < 5;
		boolean newLocationIsSameAsCurrent = newLocationHasSameAccurary
				&& newLocation.getAltitude() == mCurrentBestLocation.getAltitude()
				&& newLocation.getLongitude() == mCurrentBestLocation.getLongitude();
		if (!newLocationIsSameAsCurrent
				&& (newLocationIsSignificantlyNewer || newLocationHasSameAccurary)) return true;

		return false;
	}

}
