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

package org.projectmaxs.module.contactsread;

import org.projectmaxs.shared.global.util.Log.DebugLogSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class Settings implements OnSharedPreferenceChangeListener, DebugLogSettings {
	// App settings
	private final String DEBUG_LOG;

	private static Settings sSettings;

	public static synchronized Settings getInstance(Context context) {
		if (sSettings == null) {
			sSettings = new Settings(context);
		}
		return sSettings;
	}

	private SharedPreferences mSharedPreferences;

	private Settings(Context context) {
		// this.mSharedPreferences =
		// context.getSharedPreferences(Constants.MAIN_PACKAGE,
		// Context.MODE_PRIVATE);
		this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		DEBUG_LOG = context.getString(R.string.pref_app_debug_log_key);

		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	public boolean isDebugLogEnabled() {
		return mSharedPreferences.getBoolean(DEBUG_LOG, false);
	}

	public SharedPreferences getSharedPreferences() {
		return mSharedPreferences;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

	}

}
