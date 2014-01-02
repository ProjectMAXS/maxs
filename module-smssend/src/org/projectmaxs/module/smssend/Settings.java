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

package org.projectmaxs.module.smssend;

import org.projectmaxs.shared.global.util.Log.DebugLogSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class Settings implements OnSharedPreferenceChangeListener, DebugLogSettings {
	// App settings
	private final String DEBUG_LOG;

	private final String NOTIFY_SEND;
	private final String NOTIFY_DELIVERED;
	private final String USE_BEST_CONTACT;

	private final String DELIVERED_INTENT_REQUEST_CODE = "DELIVERED_INTENT_REQUEST_CODE";
	private final String SENT_INTENT_REQUEST_CODE = "SENT_INTENT_REQUEST_CODE";

	private static Settings sSettings;

	public static synchronized Settings getInstance(Context context) {
		if (sSettings == null) {
			sSettings = new Settings(context);
		}
		return sSettings;
	}

	private SharedPreferences mSharedPreferences;

	private Settings(Context context) {
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

		DEBUG_LOG = context.getString(R.string.pref_app_debug_log_key);
		NOTIFY_SEND = context.getString(R.string.pref_notify_send_key);
		NOTIFY_DELIVERED = context.getString(R.string.pref_notify_delivered_key);
		USE_BEST_CONTACT = context.getString(R.string.pref_use_best_contact_key);

		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	public boolean isDebugLogEnabled() {
		return mSharedPreferences.getBoolean(DEBUG_LOG, false);
	}

	public boolean notifySentEnabled() {
		return mSharedPreferences.getBoolean(NOTIFY_SEND, false);
	}

	public boolean notifyDeliveredEnabled() {
		return mSharedPreferences.getBoolean(NOTIFY_DELIVERED, false);
	}

	public boolean useBestContactEnabled() {
		return mSharedPreferences.getBoolean(USE_BEST_CONTACT, false);
	}

	public synchronized int getDeliveredIntentRequestCode(int howMany) {
		int current = mSharedPreferences.getInt(DELIVERED_INTENT_REQUEST_CODE, 0);
		mSharedPreferences.edit().putInt(DELIVERED_INTENT_REQUEST_CODE, current + howMany).commit();
		return current;
	}

	public synchronized int getSentIntentRequestCode(int howMany) {
		int current = mSharedPreferences.getInt(SENT_INTENT_REQUEST_CODE, 0);
		mSharedPreferences.edit().putInt(SENT_INTENT_REQUEST_CODE, current + howMany).commit();
		return current;
	}

	public SharedPreferences getSharedPreferences() {
		return mSharedPreferences;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

	}

}
