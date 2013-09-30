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

package org.projectmaxs.main.activities;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import org.projectmaxs.main.R;
import org.projectmaxs.main.Settings;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.main.util.FileManager;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.aidl.IFileWriteModuleService;
import org.projectmaxs.shared.global.util.AsyncServiceTask;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.PackageManagerUtil;
import org.projectmaxs.shared.global.util.SharedPreferencesUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ImportExportSettings extends Activity {

	private static final Handler HANDLER = new Handler();
	private static final Log LOG = Log.getLog();

	private static TextView sImportExportStatus;

	private EditText mImportDirectory;
	private PackageManagerUtil mPackageManagerUtil;

	public static void appendStatus(final String string) {
		// make sure the string is set on the UI (= main) thread
		HANDLER.post(new Runnable() {
			@Override
			public void run() {
				if (sImportExportStatus == null) {
					LOG.d("appendStatus: sImportExportStatus was null");
					return;
				}
				sImportExportStatus.append(string + "\n");
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.importexportsettings);
		mImportDirectory = (EditText) findViewById(R.id.importDirectory);
		sImportExportStatus = (TextView) findViewById(R.id.textImportExportStatus);
		mPackageManagerUtil = PackageManagerUtil.getInstance(this);
	}

	public void exportAll(View view) {
		sImportExportStatus.setText("");

		if (!mPackageManagerUtil.isPackageInstalled(GlobalConstants.FILEWRITE_MODULE_PACKAGE)) {
			appendStatus("Required module " + GlobalConstants.FILEWRITE_MODULE_PACKAGE
					+ " is not installed");
			return;
		}

		File exportDir = FileManager.getTimestampedSettingsExportDir();
		appendStatus("set export directory to " + exportDir.getAbsolutePath());
		File mainOutFile = new File(exportDir, Constants.MAIN_PACKAGE + ".xml");
		final String file = mainOutFile.getAbsolutePath();
		Writer writer = new CharArrayWriter();
		try {
			SharedPreferencesUtil.export(Settings.getInstance(this).getSharedPreferences(), writer,
					null);
			final byte[] bytes = writer.toString().getBytes();
			tryToExport(file, bytes, this);
		} catch (IOException e1) {
			appendStatus("could not export main settings to " + file + " error: " + e1.getMessage());
		}

		final Intent intent = new Intent(GlobalConstants.ACTION_EXPORT_SETTINGS);
		intent.putExtra(GlobalConstants.EXTRA_FILE, exportDir.getAbsolutePath());
		sendBroadcast(intent);
	}

	public void importAll(View view) {
		sImportExportStatus.setText("");

		if (!mPackageManagerUtil.isPackageInstalled("TODO")) {

		}

		File importDirectory = new File(mImportDirectory.getText().toString());
		if (!importDirectory.isDirectory()) {
			appendStatus(importDirectory.getAbsolutePath() + " is not a directory");
			return;
		}

	}

	public static void tryToExport(final String file, final byte[] bytes, Context context) {
		new AsyncServiceTask<IFileWriteModuleService>(new Intent(
				GlobalConstants.ACTION_BIND_FILEWRITE), context) {

			@Override
			public IFileWriteModuleService asInterface(IBinder iBinder) {
				return IFileWriteModuleService.Stub.asInterface(iBinder);
			}

			@Override
			public void performTask(IFileWriteModuleService iinterface) {
				String error = null;
				try {
					error = iinterface.writeFileBytes(file, bytes);
				} catch (RemoteException e) {
					error = e.getMessage();
				}
				String status;
				if (error == null) {
					status = "exported settings to " + file;
				} else {
					status = "could not export settings to " + file + " error: " + error;
				}
				appendStatus(status);
			}

		}.go();
	}
}
