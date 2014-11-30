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

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.compression.XMPPInputOutputStream;
import org.jivesoftware.smack.compression.XMPPInputOutputStream.FlushMethod;
import org.jivesoftware.smack.rosterstore.DirectoryRosterStore;
import org.jivesoftware.smack.rosterstore.RosterStore;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.jxmpp.util.XmppStringUtils;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.jul.JULHandler;
import org.projectmaxs.shared.global.util.FileUtil;
import org.projectmaxs.shared.global.util.Log.DebugLogSettings;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPSocketFactory;

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
	private static final String EXCLUDED_RESOURCES = "EXCLUDED_RESOURCES";

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
	private final String XMPP_STREAM_MANAGEMENT;
	private final String XMPP_STREAM_COMPRESSION;
	private final String XMPP_STREAM_COMPRESSION_SYNC_FLUSH;
	private final String XMPP_STREAM_ENCYPTION;
	private final String XMPP_STREAM_PRIVACY;
	private final String XMPP_STREAM_SESSION;

	// App settings
	private final String DEBUG_LOG;
	private final String XMPP_DEBUG;
	private final String DEBUG_NETWORK;
	private final String DEBUG_DNS;
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
	private XMPPTCPConnectionConfiguration mConnectionConfiguration;

	private BareJid mJidCache;
	private Set<BareJid> mMasterJidCache;

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
		XMPP_STREAM_MANAGEMENT = context.getString(R.string.pref_xmpp_stream_management_key);
		XMPP_STREAM_COMPRESSION = context.getString(R.string.pref_xmpp_stream_compression_key);
		XMPP_STREAM_COMPRESSION_SYNC_FLUSH = context
				.getString(R.string.pref_xmpp_stream_compression_sync_flush_key);
		XMPP_STREAM_ENCYPTION = context.getString(R.string.pref_xmpp_stream_encryption_key);
		XMPP_STREAM_PRIVACY = context.getString(R.string.pref_xmpp_stream_privacy_key);
		XMPP_STREAM_SESSION = context.getString(R.string.pref_xmpp_stream_session_key);
		DEBUG_NETWORK = context.getString(R.string.pref_app_debug_network_key);
		DEBUG_DNS = context.getString(R.string.pref_app_debug_dns_key);
		LAST_ACTIVE_NETWORK = context.getString(R.string.pref_app_last_active_network_key);
		XMPP_DEBUG = context.getString(R.string.pref_app_xmpp_debug_key);

		XMPP_CONNECTION_SETTINGS = new HashSet<String>(Arrays.asList(new String[] { JID, PASSWORD,
				MANUAL_SERVICE_SETTINGS, MANUAL_SERVICE_SETTINGS_HOST,
				MANUAL_SERVICE_SETTINGS_PORT, MANUAL_SERVICE_SETTINGS_SERVICE,
				XMPP_STREAM_COMPRESSION, XMPP_STREAM_ENCYPTION, XMPP_DEBUG }));

		DEBUG_LOG = context.getString(R.string.pref_app_debug_log_key);

		mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

		setDnsDebug();
	}

	public BareJid getJid() {
		if (mJidCache != null) {
			return mJidCache;
		}
		String jidString = mSharedPreferences.getString(JID, "");
		if (jidString.isEmpty()) {
			return null;
		}
		BareJid bareJid;
		try {
			bareJid = JidCreate.bareFrom(jidString);
		} catch (XmppStringprepException e) {
			throw new AssertionError(e);
		}
		return bareJid;
	}

	public void setJid(BareJid jid) {
		if (jid.hasResource()) {
			throw new IllegalArgumentException();
		}
		mSharedPreferences.edit().putString(JID, jid.toString()).commit();
		mJidCache = jid;
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
	 * @return A set containing the master JIDs.
	 */
	public Set<BareJid> getMasterJids() {
		if (mMasterJidCache != null) {
			return mMasterJidCache;
		}
		String s = mSharedPreferences.getString(MASTER_JIDS, "");
		Set<String> resString = SharedStringUtil.stringToSet(s);
		Set<BareJid> res = new HashSet<BareJid>();
		for (String jidString : resString) {
			try {
				BareJid bareJid = JidCreate.bareFrom(jidString);
				res.add(bareJid);
			} catch (XmppStringprepException e) {
				throw new AssertionError(e);
			}
		}
		return res;
	}

	public int getMasterJidCount() {
		return getMasterJids().size();
	}

	public void addMasterJid(BareJid jid) {
		Set<BareJid> masterJids = getMasterJids();
		masterJids.add(jid);
		saveMasterJids(masterJids);
	}

	public boolean removeMasterJid(BareJid jid) {
		Set<BareJid> masterJids = getMasterJids();
		if (masterJids.remove(jid)) {
			saveMasterJids(masterJids);
			return true;
		}
		return false;
	}

	public boolean isMasterJID(Jid jid) {
		BareJid bareJid = jid.asBareJidIfPossible();

		if (bareJid == null) {
			return false;
		}

		if (getMasterJids().contains(bareJid)) {
			return true;
		}

		return false;
	}

	public Set<String> getExcludedResources() {
		String s = mSharedPreferences.getString(EXCLUDED_RESOURCES, "");
		Set<String> res = SharedStringUtil.stringToSet(s);
		return res;
	}

	/**
	 * Don't sent broadcasts to certain resources. This is mostly usefull to avoid notifcation
	 * loops, i.e. when MAXS broadcasts a message and a XMPP client on the same device running MAXS
	 * receives and displays it.
	 * <p>
	 * Therefore this method current returns true if:
	 * <li>The resource starts with 'android', to exclude hangout/gtalk
	 * <li>The resource matches one of the user configured excluded resources
	 * 
	 * @param resource
	 * @return true if resource should be excluded from broadcasts
	 */
	public boolean isExcludedResource(String resource) {
		final String[] ifStartsWith = new String[] { "android" };
		for (String s : ifStartsWith) {
			if (resource.startsWith(s)) return true;
		}
		Set<String> excludedResources = getExcludedResources();
		if (excludedResources.contains(resource)) return true;
		return false;
	}

	public void addExcludedResource(String resource) {
		Set<String> excludedResources = getExcludedResources();
		excludedResources.add(resource);
		saveExcludedResources(excludedResources);
	}

	public boolean removeExcludedResource(String resource) {
		Set<String> excludedResources = getExcludedResources();
		if (excludedResources.remove(resource)) {
			saveExcludedResources(excludedResources);
			return true;
		}
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

	/**
	 * Ensures that the user configured everything so that a connection attempt can be made. If
	 * something is missing, a String describing what is missing, will be returned.
	 * 
	 * @return null if everything is fine, otherwise a string explaining what's wrong
	 */
	public String checkIfReadyToConnect() {
		if (getPassword().isEmpty()) return "Password not set or empty";
		if (getJid() == null) return "JID not set or empty";
		if (getMasterJidCount() == 0) return "Master JID(s) not configured";
		if (getManualServiceSettings()) {
			if (getManualServiceSettingsHost().isEmpty()) return "XMPP Server Host not specified";
			if (getManualServiceSettingsService().isEmpty())
				return "XMPP Server service name not specified";
		}

		return null;
	}

	public boolean isStreamManagementEnabled() {
		return mSharedPreferences.getBoolean(XMPP_STREAM_MANAGEMENT, false);
	}

	/**
	 * Retrieve a ConnectionConfiguration.
	 * 
	 * Note that because of MemorizingTrustManager, the given Context must be an instance of
	 * Application, Service or Activity
	 * 
	 * @param context
	 * @return The ConnectionConfiguration.
	 */
	public XMPPTCPConnectionConfiguration getConnectionConfiguration(Context context) {
		if (mConnectionConfiguration == null) {
			String service;
			XMPPTCPConnectionConfigurationBuilder confBuilder = XMPPTCPConnectionConfiguration
					.builder();
			if (getManualServiceSettings()) {
				String host = getManualServiceSettingsHost();
				int port = getManualServiceSettingsPort();
				service = getManualServiceSettingsService();
				confBuilder.setHost(host);
				confBuilder.setPort(port);
				confBuilder.setServiceName(service);
			} else {
				service = XmppStringUtils.parseDomain(mSharedPreferences.getString(JID, ""));
			}
			confBuilder.setUsernameAndPassword(getJid().getLocalpart(), getPassword());
			confBuilder.setServiceName(service);
			confBuilder.setResource(GlobalConstants.MAXS);
			confBuilder.setSocketFactory(XMPPSocketFactory.getInstance());

			confBuilder.setCompressionEnabled(mSharedPreferences.getBoolean(
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
				throw new IllegalArgumentException("Unknown security mode: " + securityModeString);
			}
			confBuilder.setSecurityMode(securityMode);

			confBuilder.setSendPresence(false);

			confBuilder.setDebuggerEnabled(mSharedPreferences.getBoolean(XMPP_DEBUG, false));
			confBuilder.setLegacySessionDisabled(!mSharedPreferences.getBoolean(
					XMPP_STREAM_SESSION, true));
			try {
				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, MemorizingTrustManager.getInstanceList(context), new SecureRandom());
				confBuilder.setCustomSSLContext(sc);
			} catch (NoSuchAlgorithmException e) {
				throw new IllegalStateException(e);
			} catch (KeyManagementException e) {
				throw new IllegalStateException(e);
			}

			File rosterStoreDirectory = FileUtil.getFileDir(context, "rosterStore");
			RosterStore rosterStore = DirectoryRosterStore.init(rosterStoreDirectory);
			confBuilder.setRosterStore(rosterStore);

			mConnectionConfiguration = confBuilder.build();
		}

		return mConnectionConfiguration;
	}

	public boolean privacyListsEnabled() {
		return mSharedPreferences.getBoolean(XMPP_STREAM_PRIVACY, false);
	}

	public SharedPreferences getSharedPreferences() {
		return mSharedPreferences;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		for (String s : XMPP_CONNECTION_SETTINGS) {
			if (s.equals(key)) {
				mConnectionConfiguration = null;
				break;
			}
		}
		if (key.equals(DEBUG_DNS)) {
			setDnsDebug();
		} else if (key.equals(XMPP_STREAM_COMPRESSION_SYNC_FLUSH)) {
			setSyncFlush();
		}
	}

	private void saveMasterJids(Set<BareJid> newMasterJids) {
		SharedPreferences.Editor e = mSharedPreferences.edit();

		Set<String> jidStrings = new HashSet<String>();
		for (BareJid bareJid : newMasterJids) {
			jidStrings.add(bareJid.toString());
		}
		String masterJids = SharedStringUtil.setToString(jidStrings);
		e.putString(MASTER_JIDS, masterJids);
		e.commit();
		mMasterJidCache = newMasterJids;
	}

	private void saveExcludedResources(Set<String> newExcludedResources) {
		SharedPreferences.Editor e = mSharedPreferences.edit();

		String excludedResources = SharedStringUtil.setToString(newExcludedResources);
		e.putString(EXCLUDED_RESOURCES, excludedResources);
		e.commit();
	}

	private boolean getManualServiceSettings() {
		return mSharedPreferences.getBoolean(MANUAL_SERVICE_SETTINGS, false);
	}

	private String getManualServiceSettingsHost() {
		return mSharedPreferences.getString(MANUAL_SERVICE_SETTINGS_HOST, "");
	}

	private int getManualServiceSettingsPort() {
		return Integer.parseInt(mSharedPreferences.getString(MANUAL_SERVICE_SETTINGS_PORT, "5222"));
	}

	private String getManualServiceSettingsService() {
		return mSharedPreferences.getString(MANUAL_SERVICE_SETTINGS_SERVICE, "");
	}

	private void setDnsDebug() {
		final String minidnsPkg = "de.measite.minidns";
		if (mSharedPreferences.getBoolean(DEBUG_DNS, false)) {
			JULHandler.removeNoLogPkg(minidnsPkg);
		} else {
			JULHandler.addNoLogPkg(minidnsPkg);
		}
	}

	private void setSyncFlush() {
		if (mSharedPreferences.getBoolean(XMPP_STREAM_COMPRESSION_SYNC_FLUSH, false)) {
			XMPPInputOutputStream.setFlushMethod(FlushMethod.SYNC_FLUSH);
		} else {
			XMPPInputOutputStream.setFlushMethod(FlushMethod.FULL_FLUSH);
		}
	}
}
