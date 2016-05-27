/*
    This file is part of Project MAXS.

    MAXS and its Transports is free software: you can redistribute it and/or modify
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

package org.projectmaxs.shared.transport;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Set;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.jul.JULHandler;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.PermissionUtil;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.maintransport.TransportInformation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import eu.geekplace.iesp.ImportExportSharedPreferences;

public abstract class MAXSTransportReceiver extends BroadcastReceiver {
	static {
		JULHandler.setAsDefaultUncaughtExceptionHandler();
	}

	private final Log mLog;
	private final TransportInformation mTransportInformation;

	public MAXSTransportReceiver(Log log, TransportInformation transportInformation) {
		mLog = log;
		mTransportInformation = transportInformation;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		initLog(context);
		String action = intent.getAction();
		mLog.d("onReceive: action=" + action);

		String replyToClassName;
		Intent replyIntent = null;
		if (GlobalConstants.ACTION_REGISTER.equals(action)) {
			replyIntent = new Intent(TransportConstants.ACTION_REGISTER_TRANSPORT);
			replyIntent.putExtra(TransportConstants.EXTRA_TRANSPORT_INFORMATION,
					mTransportInformation);
			replyToClassName = TransportConstants.MAIN_TRANSPORT_SERVICE;
		} else if (GlobalConstants.ACTION_EXPORT_SETTINGS.equals(action)) {
			String directory = intent.getStringExtra(GlobalConstants.EXTRA_FILE);
			replyIntent = exportSettings(context, directory);
			replyToClassName = GlobalConstants.MAIN_INTENT_SERVICE;
		} else if (GlobalConstants.ACTION_IMPORT_SETTINGS.equals(action)) {
			String settings = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			replyIntent = importSettings(context, settings);
			replyToClassName = GlobalConstants.MAIN_INTENT_SERVICE;
		} else {
			throw new IllegalStateException("MAXSTransportReceiver: unknown action=" + action);
		}
		replyIntent.setClassName(TransportConstants.MAIN_PACKAGE, replyToClassName);

		// The transport was just installed and MAXS main send an ACTION_REGISTER intent. This
		// is the ideal time to check if we are on Android 6.0 or higher and thus require to ask
		// the user for certain permissions.
		boolean permOk = PermissionUtil.checkAndRequestIfNecessary(context, replyIntent);
		if (!permOk) {
			mLog.i("Not replying with " + replyIntent.getAction() + " to " + action
					+ " because my permissions are not OK.");
			return;
		}

		mLog.d("onReceive: replying with action=" + replyIntent.getAction());
		context.startService(replyIntent);
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
				directory + "/" + mTransportInformation.getTransportPackage() + ".xml");
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
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, mTransportInformation.getTransportPackage()
				+ ": " + status);
		return intent;
	}
}
