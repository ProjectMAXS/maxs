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
import java.util.Set;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.jul.JULHandler;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.MainModuleConstants;
import org.projectmaxs.shared.mainmodule.ModuleInformation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import eu.geekplace.iesp.ImportExportSharedPreferences;

public abstract class MAXSModuleReceiver extends BroadcastReceiver {
	static {
		JULHandler.setAsDefaultUncaughtExceptionHandler();
	}

	private final Log mLog;
	private final ModuleInformation mModuleInformation;
	private final SupraCommand[] mCommands;

	private Context mContext;

	public MAXSModuleReceiver(Log log, ModuleInformation moduleInformation) {
		mLog = log;
		mModuleInformation = moduleInformation;
		mCommands = new SupraCommand[0];
	}

	public MAXSModuleReceiver(Log log, ModuleInformation moduleInformation,
			final SupraCommand[] commands) {
		mLog = log;
		mModuleInformation = moduleInformation;
		mCommands = commands;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		init(context);
		String action = intent.getAction();
		mLog.d("onReceive: action=" + action);

		String replyToClassName;
		Intent replyIntent = null;
		if (GlobalConstants.ACTION_REGISTER.equals(action)) {
			replyIntent = new Intent(GlobalConstants.ACTION_REGISTER_MODULE);
			replyIntent.putExtra(GlobalConstants.EXTRA_MODULE_INFORMATION, mModuleInformation);
			replyToClassName = MainModuleConstants.MAIN_MODULE_SERVICE;
		} else if (GlobalConstants.ACTION_EXPORT_SETTINGS.equals(action)) {
			String directory = intent.getStringExtra(GlobalConstants.EXTRA_FILE);
			replyIntent = exportSettings(context, directory);
			replyToClassName = GlobalConstants.MAIN_INTENT_SERVICE;
		} else if (GlobalConstants.ACTION_IMPORT_SETTINGS.equals(action)) {
			String settings = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			replyIntent = importSettings(context, settings);
			replyToClassName = GlobalConstants.MAIN_INTENT_SERVICE;
		} else {
			throw new IllegalStateException("MAXSModuleReceiver: unknown action=" + action);
		}
		replyIntent.setClassName(GlobalConstants.MAIN_PACKAGE, replyToClassName);
		mLog.d("onReceive: replying with action=" + replyIntent.getAction());
		context.startService(replyIntent);
	}

	private final void init(Context context) {
		if (mContext != null) return;

		mContext = context;
		for (SupraCommand command : mCommands)
			command.addTo(mModuleInformation, context);

		initLog(context);
	}

	public abstract void initLog(Context context);

	public abstract SharedPreferences getSharedPreferences(Context context);

	public Set<String> doNotExport() {
		return null;
	}

	private Intent exportSettings(Context context, String directory) {
		StringWriter writer = new StringWriter();
		try {
			ImportExportSharedPreferences.export(getSharedPreferences(context), writer,
					doNotExport());
		} catch (IOException e) {
			mLog.e("exportSettings", e);
			return importExportStatus(e.getMessage());
		}
		Intent intent = new Intent(GlobalConstants.ACTION_EXPORT_TO_FILE);
		intent.putExtra(GlobalConstants.EXTRA_FILE,
				directory + "/" + mModuleInformation.getModulePackage() + ".xml");
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, writer.toString());
		return intent;
	}

	private Intent importSettings(Context context, String settings) {
		try {
			ImportExportSharedPreferences.importFromReader(getSharedPreferences(context),
					new StringReader(settings));
		} catch (Exception e) {
			mLog.e("importSettings", e);
			return importExportStatus(e.getMessage());
		}
		return importExportStatus("Imported");
	}

	private Intent importExportStatus(String status) {
		Intent intent = new Intent(GlobalConstants.ACTION_IMPORT_EXPORT_STATUS);
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, mModuleInformation.getModulePackage() + ": "
				+ status);
		return intent;
	}
}
