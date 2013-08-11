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

package org.projectmaxs.main;

import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.Log.LogSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class Settings implements OnSharedPreferenceChangeListener {

	private static final String LAST_RECIPIENT = "LAST_RECIPIENT";
	private static final String CMD_ID = "CMD_ID";
	private static final String SERVICE_ACTIVE = "SERVICE_ACTIVE";

	// App settings
	private final String DEBUG_LOG;
	private final String CONNECT_ON_MAIN_SCREEN;
	private final String CONNECT_ON_BOOT_COMPLETED;

	private static Settings sSettings;

	public static Settings getInstance(Context context) {
		if (sSettings == null) {
			sSettings = new Settings(context);
		}
		return sSettings;
	}

	private SharedPreferences mSharedPreferences;
	private LogSettings mLogSettings;

	private Settings(Context context) {
		// this.mSharedPreferences =
		// context.getSharedPreferences(Constants.MAIN_PACKAGE,
		// Context.MODE_PRIVATE);
		this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		DEBUG_LOG = context.getString(R.string.pref_app_debug_log_key);
		CONNECT_ON_MAIN_SCREEN = context.getString(R.string.pref_app_connect_on_main_screen_key);
		CONNECT_ON_BOOT_COMPLETED = context.getString(R.string.pref_app_connect_on_boot_completed_key);

		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mLogSettings = new Log.LogSettings() {
			@Override
			public boolean debugLog() {
				return isDebugLogEnabled();
			}
		};
	}

	public void setLastRecipient(String lastRecipient) {
		mSharedPreferences.edit().putString(LAST_RECIPIENT, lastRecipient).commit();
	}

	public String getLastRecipient() {
		return mSharedPreferences.getString(LAST_RECIPIENT, "");
	}

	public int getNextCommandId() {
		int id = mSharedPreferences.getInt(CMD_ID, 0);
		mSharedPreferences.edit().putInt(CMD_ID, id + 1).commit();
		return id;
	}

	public void setServiceState(boolean active) {
		mSharedPreferences.edit().putBoolean(SERVICE_ACTIVE, active).commit();
	}

	public boolean getServiceState() {
		return mSharedPreferences.getBoolean(SERVICE_ACTIVE, false);
	}

	public boolean connectOnMainScreen() {
		return mSharedPreferences.getBoolean(CONNECT_ON_MAIN_SCREEN, false);
	}

	public boolean connectOnBootCompleted() {
		return mSharedPreferences.getBoolean(CONNECT_ON_BOOT_COMPLETED, false);
	}

	public boolean isDebugLogEnabled() {
		return mSharedPreferences.getBoolean(DEBUG_LOG, false);
	}

	public Log.LogSettings getLogSettings() {
		return mLogSettings;
	}

	public SharedPreferences getSharedPreferences() {
		return mSharedPreferences;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

	}

}
