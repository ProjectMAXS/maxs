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

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;
import org.projectmaxs.main.util.StringUtil;
import org.projectmaxs.main.xmpp.XMPPSocketFactory;
import org.projectmaxs.shared.util.Log;
import org.projectmaxs.shared.util.Log.LogSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.preference.PreferenceManager;

public class Settings implements OnSharedPreferenceChangeListener {

	private static final String MASTER_JIDS = "MASTER_JIDS";
	private static final String JID = "JID";
	private static final String PASSWORD = "PASSWORD";
	private static final String LAST_RECIPIENT = "LAST_RECIPIENT";
	private static final String CMD_ID = "CMD_ID";
	private static final String CONNECTION_STATE = "CONNECTION_STATE";

	/**
	 * A set of keys that should not get exported
	 */
	// @formatter:off
	public static final Set<String> DO_NOT_EXPORT = new HashSet<String>(Arrays.asList(new String[] { 
			PASSWORD
			}));
	// @formatter:on

	// XMPP settings
	private final String MANUAL_SERVICE_SETTINGS;
	private final String MANUAL_SERVICE_SETTINGS_HOST;
	private final String MANUAL_SERVICE_SETTINGS_PORT;
	private final String MANUAL_SERVICE_SETTINGS_SERVICE;
	private final String XMPP_STREAM_COMPRESSION;
	private final String XMPP_STREAM_ENCYPTION;

	// App settings
	private final String DEBUG_LOG;
	private final String XMPP_DEBUG;
	private final String CONNECT_ON_MAIN_SCREEN;

	private final Set<String> XMPP_CONNECTION_SETTINGS;

	private static Settings sSettings;

	public static Settings getInstance(Context context) {
		if (sSettings == null) {
			sSettings = new Settings(context);
		}
		return sSettings;
	}

	private SharedPreferences mSharedPreferences;
	private ConnectionConfiguration mConnectionConfiguration;
	private LogSettings mLogSettings;

	private Settings(Context context) {
		// this.mSharedPreferences =
		// context.getSharedPreferences(Constants.MAIN_PACKAGE,
		// Context.MODE_PRIVATE);
		this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		MANUAL_SERVICE_SETTINGS = context.getString(R.string.pref_manual_service_settings_key);
		MANUAL_SERVICE_SETTINGS_HOST = context.getString(R.string.pref_manual_service_settings_host_key);
		MANUAL_SERVICE_SETTINGS_PORT = context.getString(R.string.pref_manual_service_settings_port_key);
		MANUAL_SERVICE_SETTINGS_SERVICE = context.getString(R.string.pref_manual_service_settings_service_key);
		XMPP_STREAM_COMPRESSION = context.getString(R.string.pref_xmpp_stream_compression_key);
		XMPP_STREAM_ENCYPTION = context.getString(R.string.pref_xmpp_stream_encryption_key);

		XMPP_CONNECTION_SETTINGS = new HashSet<String>(Arrays.asList(new String[] { JID, PASSWORD,
				MANUAL_SERVICE_SETTINGS, MANUAL_SERVICE_SETTINGS_HOST, MANUAL_SERVICE_SETTINGS_PORT,
				MANUAL_SERVICE_SETTINGS_SERVICE, XMPP_STREAM_COMPRESSION, XMPP_STREAM_ENCYPTION }));

		DEBUG_LOG = context.getString(R.string.pref_app_debug_log_key);
		XMPP_DEBUG = context.getString(R.string.pref_app_xmpp_debug_key);
		CONNECT_ON_MAIN_SCREEN = context.getString(R.string.pref_app_connect_on_main_screen_key);

		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		mLogSettings = new Log.LogSettings() {
			@Override
			public boolean debugLog() {
				return isDebugLogEnabled();
			}
		};
	}

	public String getJid() {
		return mSharedPreferences.getString(JID, "");
	}

	public void setJid(String jid) {
		mSharedPreferences.edit().putString(JID, jid).commit();
	}

	public String getPassword() {
		return mSharedPreferences.getString(PASSWORD, "");
	}

	public void setPassword(String password) {
		mSharedPreferences.edit().putString(PASSWORD, password).commit();
	}

	/**
	 * Returns a set of master JID Strings or an empty set if no master JID was
	 * ever set.
	 * 
	 * @return
	 */
	public Set<String> getMasterJids() {
		String s = mSharedPreferences.getString(MASTER_JIDS, "");
		Set<String> res = StringUtil.stringToSet(s);
		return res;
	}

	public int getMasterJidCount() {
		return getMasterJids().size();
	}

	public void addMasterJid(String jid) {
		Set<String> masterJids = getMasterJids();
		masterJids.add(jid);
		saveMasterJids(masterJids);
	}

	public boolean removeMasterJid(String jid) {
		Set<String> masterJids = getMasterJids();
		if (masterJids.remove(jid)) {
			saveMasterJids(masterJids);
			return true;
		}
		return false;
	}

	public boolean isMasterJID(String jid) {
		String bareJID = StringUtils.parseBareAddress(jid);
		Set<String> masterJids = getMasterJids();
		for (String s : masterJids)
			if (s.equals(bareJID)) return true;

		return false;
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

	public void setXMPPConnectionState(boolean active) {
		mSharedPreferences.edit().putBoolean(CONNECTION_STATE, active).commit();
	}

	public boolean getXMPPConnectionState() {
		return mSharedPreferences.getBoolean(CONNECTION_STATE, false);
	}

	public boolean connectOnMainScreen() {
		return mSharedPreferences.getBoolean(CONNECT_ON_MAIN_SCREEN, false);
	}

	public boolean isDebugLogEnabled() {
		return mSharedPreferences.getBoolean(DEBUG_LOG, false);
	}

	public Log.LogSettings getLogSettings() {
		return mLogSettings;
	}

	public ConnectionConfiguration getConnectionConfiguration() throws XMPPException {
		if (mConnectionConfiguration == null) {
			if (mSharedPreferences.getBoolean(MANUAL_SERVICE_SETTINGS, false)) {
				String host = mSharedPreferences.getString(MANUAL_SERVICE_SETTINGS_HOST, "");
				int port = Integer.parseInt(mSharedPreferences.getString(MANUAL_SERVICE_SETTINGS_PORT, "5222"));
				String service = mSharedPreferences.getString(MANUAL_SERVICE_SETTINGS_SERVICE, "");
				mConnectionConfiguration = new ConnectionConfiguration(host, port, service);
			}
			else {
				String service = StringUtils.parseServer(mSharedPreferences.getString(JID, ""));
				mConnectionConfiguration = new AndroidConnectionConfiguration(service, 1234);
			}
			mConnectionConfiguration.setSocketFactory(XMPPSocketFactory.getInstance());

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				mConnectionConfiguration.setTruststoreType("AndroidCAStore");
				mConnectionConfiguration.setTruststorePassword(null);
				mConnectionConfiguration.setTruststorePath(null);
			}
			else {
				mConnectionConfiguration.setTruststoreType("BKS");
				String path = System.getProperty("javax.net.ssl.trustStore");
				if (path == null) {
					path = System.getProperty("java.home") + File.separator + "etc" + File.separator + "security"
							+ File.separator + "cacerts.bks";
				}
				mConnectionConfiguration.setTruststorePath(path);
			}

			mConnectionConfiguration.setCompressionEnabled(mSharedPreferences
					.getBoolean(XMPP_STREAM_COMPRESSION, false));

			ConnectionConfiguration.SecurityMode securityMode;
			if (mSharedPreferences.getBoolean(XMPP_STREAM_ENCYPTION, false)) {
				securityMode = ConnectionConfiguration.SecurityMode.required;
			}
			else {
				securityMode = ConnectionConfiguration.SecurityMode.disabled;
			}
			mConnectionConfiguration.setSecurityMode(securityMode);

			mConnectionConfiguration.setReconnectionAllowed(false);
		}

		return mConnectionConfiguration;
	}

	public SharedPreferences getSharedPreferences() {
		return mSharedPreferences;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		for (String s : XMPP_CONNECTION_SETTINGS)
			if (s.equals(key)) mConnectionConfiguration = null;

		if (key.equals(XMPP_DEBUG)) Connection.DEBUG_ENABLED = sharedPreferences.getBoolean(XMPP_DEBUG, false);
	}

	private void saveMasterJids(Set<String> newMasterJids) {
		SharedPreferences.Editor e = mSharedPreferences.edit();

		String masterJids = StringUtil.setToString(newMasterJids);
		e.putString(MASTER_JIDS, masterJids);
		e.commit();
	}
}
