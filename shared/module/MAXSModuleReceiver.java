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

package org.projectmaxs.shared.module;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.SharedPreferencesUtil;
import org.projectmaxs.shared.mainmodule.ModuleInformation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public abstract class MAXSModuleReceiver extends BroadcastReceiver {
	private final Log mLog;
	private final ModuleInformation mModuleInformation;

	public MAXSModuleReceiver(Log log, ModuleInformation moduleInformation) {
		mLog = log;
		mModuleInformation = moduleInformation;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		initLog(context);
		String action = intent.getAction();
		mLog.d("onReceive: action=" + action);

		Intent replyIntent = null;
		if (GlobalConstants.ACTION_REGISTER.equals(action)) {
			replyIntent = new Intent(GlobalConstants.ACTION_REGISTER_MODULE);
			replyIntent.putExtra(GlobalConstants.EXTRA_MODULE_INFORMATION, mModuleInformation);
			replyIntent.putExtra(GlobalConstants.EXTRA_TYPE, "module");
		}
		else if (GlobalConstants.ACTION_EXPORT_SETTINGS.equals(action)) {
			String directory = intent.getStringExtra(GlobalConstants.EXTRA_FILE);
			replyIntent = exportSettings(context, directory);
		}
		else if (GlobalConstants.ACTION_IMPORT_SETTINGS.equals(action)) {
			String settings = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			replyIntent = importSettings(context, settings);
		}
		if (replyIntent != null) {
			mLog.d("onReceive: replying with action=" + replyIntent.getAction());
			context.startService(replyIntent);
		}
	}

	public abstract void initLog(Context context);

	public abstract SharedPreferences getSharedPreferences(Context context);

	private Intent exportSettings(Context context, String directory) {
		StringWriter writer = new StringWriter();
		try {
			SharedPreferencesUtil.export(getSharedPreferences(context), writer, null);
		} catch (IOException e) {
			mLog.e("exportSettings", e);
			return importExportStatus(e.getMessage());
		}
		Intent intent = new Intent(GlobalConstants.ACTION_EXPORT_TO_FILE);
		intent.putExtra(GlobalConstants.EXTRA_FILE, directory + "/" + mModuleInformation.getModulePackage() + ".xml");
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, writer.toString());
		return intent;
	}

	private Intent importSettings(Context context, String settings) {
		try {
			SharedPreferencesUtil.importFromReader(getSharedPreferences(context), new StringReader(settings));
		} catch (Exception e) {
			mLog.e("importSettings", e);
			return importExportStatus(e.getMessage());
		}
		return importExportStatus("Imported");
	}

	private Intent importExportStatus(String status) {
		Intent intent = new Intent(GlobalConstants.ACTION_IMPORT_EXPORT_STATUS);
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, mModuleInformation.getModulePackage() + ": " + status);
		return intent;
	}
}
