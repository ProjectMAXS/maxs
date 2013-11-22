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

package org.projectmaxs.transport.xmpp;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;
import org.projectmaxs.shared.global.util.Log.DebugLogSettings;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPSocketFactory;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import de.duenndns.ssl.MemorizingTrustManager;

public class Settings implements OnSharedPreferenceChangeListener, DebugLogSettings {

	private static final String MASTER_JIDS = "MASTER_JIDS";
	private static final String JID = "JID";
	private static final String PASSWORD = "PASSWORD";
	private static final String LAST_RECIPIENT = "LAST_RECIPIENT";
	private static final String CMD_ID = "CMD_ID";
	private static final String STATUS = "STATUS";

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
	private final String DEBUG_NETWORK;
	private final String LAST_ACTIVE_NETWORK;

	private final Set<String> XMPP_CONNECTION_SETTINGS;

	private static Settings sSettings;

	public static synchronized Settings getInstance(Context context) {
		if (sSettings == null) {
			sSettings = new Settings(context);
		}
		return sSettings;
	}

	private SharedPreferences mSharedPreferences;
	private ConnectionConfiguration mConnectionConfiguration;

	private Settings(Context context) {
		// this.mSharedPreferences =
		// context.getSharedPreferences(Constants.MAIN_PACKAGE,
		// Context.MODE_PRIVATE);
		this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		MANUAL_SERVICE_SETTINGS = context.getString(R.string.pref_manual_service_settings_key);
		MANUAL_SERVICE_SETTINGS_HOST = context
				.getString(R.string.pref_manual_service_settings_host_key);
		MANUAL_SERVICE_SETTINGS_PORT = context
				.getString(R.string.pref_manual_service_settings_port_key);
		MANUAL_SERVICE_SETTINGS_SERVICE = context
				.getString(R.string.pref_manual_service_settings_service_key);
		XMPP_STREAM_COMPRESSION = context.getString(R.string.pref_xmpp_stream_compression_key);
		XMPP_STREAM_ENCYPTION = context.getString(R.string.pref_xmpp_stream_encryption_key);
		DEBUG_NETWORK = context.getString(R.string.pref_app_debug_network_key);
		LAST_ACTIVE_NETWORK = context.getString(R.string.pref_app_last_active_network_key);

		XMPP_CONNECTION_SETTINGS = new HashSet<String>(Arrays.asList(new String[] { JID, PASSWORD,
				MANUAL_SERVICE_SETTINGS, MANUAL_SERVICE_SETTINGS_HOST,
				MANUAL_SERVICE_SETTINGS_PORT, MANUAL_SERVICE_SETTINGS_SERVICE,
				XMPP_STREAM_COMPRESSION, XMPP_STREAM_ENCYPTION }));

		DEBUG_LOG = context.getString(R.string.pref_app_debug_log_key);
		XMPP_DEBUG = context.getString(R.string.pref_app_xmpp_debug_key);

		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
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
		Set<String> res = SharedStringUtil.stringToSet(s);
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

	public boolean isDebugLogEnabled() {
		return mSharedPreferences.getBoolean(DEBUG_LOG, false);
	}

	public void setLastActiveNetwork(String network) {
		mSharedPreferences.edit().putString(LAST_ACTIVE_NETWORK, network).commit();
	}

	public String getLastActiveNetwork() {
		return mSharedPreferences.getString(LAST_ACTIVE_NETWORK, "");
	}

	public boolean isNetworkDebugLogEnabled() {
		return mSharedPreferences.getBoolean(DEBUG_NETWORK, false);
	}

	public void setStatus(String status) {
		mSharedPreferences.edit().putString(STATUS, status).commit();
	}

	public String getStatus() {
		return mSharedPreferences.getString(STATUS, "");
	}

	public ConnectionConfiguration getConnectionConfiguration(Application application)
			throws XMPPException {
		if (mConnectionConfiguration == null) {
			if (mSharedPreferences.getBoolean(MANUAL_SERVICE_SETTINGS, false)) {
				String host = mSharedPreferences.getString(MANUAL_SERVICE_SETTINGS_HOST, "");
				int port = Integer.parseInt(mSharedPreferences.getString(
						MANUAL_SERVICE_SETTINGS_PORT, "5222"));
				String service = mSharedPreferences.getString(MANUAL_SERVICE_SETTINGS_SERVICE, "");
				mConnectionConfiguration = new ConnectionConfiguration(host, port, service);
			} else {
				String service = StringUtils.parseServer(mSharedPreferences.getString(JID, ""));
				mConnectionConfiguration = new AndroidConnectionConfiguration(service, 1234);
			}
			mConnectionConfiguration.setSocketFactory(XMPPSocketFactory.getInstance());

			mConnectionConfiguration.setCompressionEnabled(mSharedPreferences.getBoolean(
					XMPP_STREAM_COMPRESSION, false));

			ConnectionConfiguration.SecurityMode securityMode;
			final String securityModeString = mSharedPreferences.getString(XMPP_STREAM_ENCYPTION,
					"opt");
			if ("opt".equals(securityModeString)) {
				securityMode = ConnectionConfiguration.SecurityMode.enabled;
			} else if ("req".equals(securityModeString)) {
				securityMode = ConnectionConfiguration.SecurityMode.required;
			} else if ("dis".equals(securityModeString)) {
				securityMode = ConnectionConfiguration.SecurityMode.disabled;
			} else {
				throw new IllegalArgumentException("Unkown security mode: " + securityModeString);
			}
			mConnectionConfiguration.setSecurityMode(securityMode);

			mConnectionConfiguration.setReconnectionAllowed(false);
			mConnectionConfiguration.setSendPresence(false);

			try {
				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, MemorizingTrustManager.getInstanceList(application),
						new SecureRandom());
				mConnectionConfiguration.setCustomSSLContext(sc);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException(e);
			} catch (KeyManagementException e) {
				throw new IllegalStateException(e);
			}
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

		if (key.equals(XMPP_DEBUG))
			Connection.DEBUG_ENABLED = sharedPreferences.getBoolean(XMPP_DEBUG, false);
	}

	private void saveMasterJids(Set<String> newMasterJids) {
		SharedPreferences.Editor e = mSharedPreferences.edit();

		String masterJids = SharedStringUtil.setToString(newMasterJids);
		e.putString(MASTER_JIDS, masterJids);
		e.commit();
	}
}
