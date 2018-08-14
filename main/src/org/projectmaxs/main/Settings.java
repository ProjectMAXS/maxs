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

import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.util.Log.DebugLogSettings;
import org.projectmaxs.shared.mainmodule.RecentContact;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class Settings implements OnSharedPreferenceChangeListener, DebugLogSettings {

	private static final String CMD_ID = "CMD_ID";
	private static final String SERVICE_ACTIVE = "SERVICE_ACTIVE";
	private static final String RECENT_CONTACT_INFO = "RECENT_CONTACT_INFO";
	private static final String RECENT_CONTACT_DISPLAY_NAME = "RECENT_CONTACT_DISPLAY_NAME";
	private static final String RECENT_CONTACT_LOOKUP_KEY = "RECENT_CONTACT_LOOKUP_KEY";
	private static final String PERM_CHECK_TIMESTAMP_KEY = "PERM_CHECK_TIMESTAMP_KEY";

	// App settings
	private final String DEBUG_LOG;
	private final String CONNECT_ON_MAIN_SCREEN;
	private final String CONNECT_ON_BOOT_COMPLETED;

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
		CONNECT_ON_MAIN_SCREEN = context.getString(R.string.pref_app_connect_on_main_screen_key);
		CONNECT_ON_BOOT_COMPLETED = context
				.getString(R.string.pref_app_connect_on_boot_completed_key);

		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	public int getNextCommandId() {
		int id = mSharedPreferences.getInt(CMD_ID, 0);
		mSharedPreferences.edit().putInt(CMD_ID, id + 1).apply();
		return id;
	}

	public void setServiceState(boolean active) {
		mSharedPreferences.edit().putBoolean(SERVICE_ACTIVE, active).apply();
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

	public void setRecentContact(RecentContact recentContact) {
		String recentContactInfo = recentContact.mContactInfo;
		String displayName = null;
		String lookupKey = null;
		if (recentContact.mContact != null) {
			displayName = recentContact.mContact.getDisplayName();
			lookupKey = recentContact.mContact.getLookupKey();
		}
		mSharedPreferences.edit().putString(RECENT_CONTACT_INFO, recentContactInfo)
				.putString(RECENT_CONTACT_DISPLAY_NAME, displayName)
				.putString(RECENT_CONTACT_LOOKUP_KEY, lookupKey).apply();
	}

	public RecentContact getRecentContact() {
		String recentContactInfo = mSharedPreferences.getString(RECENT_CONTACT_INFO, null);
		if (recentContactInfo == null) return null;
		String displayName = mSharedPreferences.getString(RECENT_CONTACT_DISPLAY_NAME, null);
		if (displayName == null) {
			return new RecentContact(recentContactInfo);
		} else {
			String lookupKey = mSharedPreferences.getString(RECENT_CONTACT_LOOKUP_KEY, null);
			Contact contact = new Contact(displayName, lookupKey);
			return new RecentContact(recentContactInfo, contact);
		}
	}

	public long getPermCheckTimestamp() {
		return mSharedPreferences.getLong(PERM_CHECK_TIMESTAMP_KEY, -1);
	}

	public boolean setPermCheckTimestamp(long value) {
		return mSharedPreferences.edit().putLong(PERM_CHECK_TIMESTAMP_KEY, value).commit();
	}

	public SharedPreferences getSharedPreferences() {
		return mSharedPreferences;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

	}

}
