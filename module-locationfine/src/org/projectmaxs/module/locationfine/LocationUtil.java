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

package org.projectmaxs.module.locationfine;

import java.util.ArrayList;
import java.util.List;

import org.projectmaxs.shared.module.SharedLocationUtil;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationManager;

public class LocationUtil {

	@SuppressLint("MissingPermission")
	public static List<Location> getLastKnownLocations(LocationManager locationManager) {
		List<String> providers = locationManager.getAllProviders();
		List<Location> lastKnownLocations = new ArrayList<>(providers.size());
		for (String provider : providers) {
			Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
			if (lastKnownLocation == null) continue;
			lastKnownLocations.add(lastKnownLocation);
		}
		return lastKnownLocations;
	}

	public static List<Location> getLastKnownLocationsSorted(LocationManager locationManager) {
		List<Location> locations = getLastKnownLocations(locationManager);
		SharedLocationUtil.sort(locations);
		return locations;
	}

}
