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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jivesoftware.smack.util.StringUtils;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.shared.util.Log;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

	private static final String MASTER_JIDS = "MASTER_JIDS";
	private static final String JID = "JID";
	private static final String PASSWORD = "PASSWORD";
	private static final String LAST_RECIPIENT = "LAST_RECIPIENT";
	private static final String CMD_ID = "CMD_ID";
	private static final String CONNECTION_STATE = "CONNECTION_STATE";

	private static Settings sSettings;

	public static Settings getInstance(Context ctx) {
		if (sSettings == null) {
			sSettings = new Settings(ctx);
		}
		return sSettings;
	}

	private Context mCtx;
	private SharedPreferences mSharedPreferences;

	private boolean debugLog = true;

	private Settings(Context ctx) {
		this.mCtx = ctx;
		this.mSharedPreferences = ctx.getSharedPreferences(Constants.MAIN_PACKAGE, Context.MODE_PRIVATE);
	}

	public boolean connectionSettingsObsolete() {
		// TODO
		return true;
	}

	public void resetConnectionSettingsObsolete() {
		// TODO
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

	public boolean manualServerSettings() {
		return true;
	}

	public String serverHost() {
		return "mate.freakempire.de";
	}

	public int serverPort() {
		return 5222;
	}

	public String serviceName() {
		return "freakempire.de";
	}

	/**
	 * Returns a set of master JID Strings or an empty set if no master JID was
	 * ever set.
	 * 
	 * @return
	 */
	public Set<String> getMasterJids() {
		String s = mSharedPreferences.getString(MASTER_JIDS, "");
		Set<String> res = new HashSet<String>();
		if (!s.equals("")) {
			res.addAll(Arrays.asList(s.split(" ")));
		}
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

	public void setXMPPConnectionState(XMPPService.State state) {
		mSharedPreferences.edit().putInt(CONNECTION_STATE, state.ordinal()).commit();
	}

	public XMPPService.State getXMPPConnectionState() {
		int stateInt = mSharedPreferences.getInt(CONNECTION_STATE, XMPPService.State.Disconnected.ordinal());
		return XMPPService.State.values()[stateInt];
	}

	public Log.LogSettings getLogSettings() {
		return new Log.LogSettings() {

			@Override
			public boolean debugLog() {
				return debugLog;
			}

		};
	}

	private void saveMasterJids(Set<String> newMasterJids) {
		SharedPreferences.Editor e = mSharedPreferences.edit();

		StringBuilder sb = new StringBuilder();
		for (String s : newMasterJids) {
			sb.append(s);
			sb.append(" ");
		}

		e.putString(MASTER_JIDS, sb.toString());
		e.commit();
	}
}
