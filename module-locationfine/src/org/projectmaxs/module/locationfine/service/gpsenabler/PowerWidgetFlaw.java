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

package org.projectmaxs.module.locationfine.service.gpsenabler;

import org.projectmaxs.module.locationfine.service.LocationService;

import android.content.Intent;
import android.net.Uri;

/**
 * Security hole from http://code.google.com/p/android/issues/detail?id=7890
 */
public class PowerWidgetFlaw implements GpsEnablerDisabler {

	@Override
	public void enableGps(LocationService service) {
		// Method 1, Security hole from
		final Intent intent = new Intent();
		intent.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		intent.setData(Uri.parse("3"));
		service.sendBroadcast(intent);
	}

	@Override
	public void disableGps(LocationService service) {
		// this method is basically a switch
		enableGps(service);
	}

}
