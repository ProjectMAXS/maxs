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
import java.io.StringReader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.main.ModuleRegistry;
import org.projectmaxs.main.R;
import org.projectmaxs.main.Settings;
import org.projectmaxs.main.TransportRegistry;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.main.util.FileManager;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.aidl.IFileReadModuleService;
import org.projectmaxs.shared.global.aidl.IFileWriteModuleService;
import org.projectmaxs.shared.global.util.AsyncServiceTask;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.PackageManagerUtil;
import org.projectmaxs.shared.global.util.SharedPreferencesUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ImportExportSettings extends Activity {

	private static final Handler HANDLER = new Handler();
	private static final Log LOG = Log.getLog();
	private static final String PICK_DIRECTORY_INTENT = "org.openintents.action.PICK_DIRECTORY";
	private static final String OIFM_PACKAGE = "org.openintents.filemanager";
	private static final Uri OIFM_MARKET_URI = Uri.parse("market://search?q=pname:" + OIFM_PACKAGE);
	private static final Uri OIFM_FDROID_URI = Uri
			.parse("https://f-droid.org/repository/browse/?fdid=" + OIFM_PACKAGE);
	private static final int FILE_REQUEST_CODE = 1;

	private static TextView sImportExportStatus;

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
		final Intent intent = new Intent(PICK_DIRECTORY_INTENT);
		if (!mPackageManagerUtil.isIntentAvailable(intent)) {
			displayFileManagerInstallDialog();
			return;
		}
		startActivityForResult(intent, FILE_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case FILE_REQUEST_CODE:
			if (intent == null || intent.getData() == null || intent.getData().toString().isEmpty()) {
				appendStatus("No directory path name received");
				return;
			}
			String importDirectory = intent.getData().getPath();
			tryToImport(importDirectory);
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void tryToImport(final String directory) {
		// Start with trying to import main's settings
		new AsyncServiceTask<IFileReadModuleService>(new Intent(
				GlobalConstants.ACTION_BIND_FILEREAD), this) {

			@Override
			public IFileReadModuleService asInterface(IBinder iBinder) {
				return IFileReadModuleService.Stub.asInterface(iBinder);
			}

			@Override
			public void performTask(IFileReadModuleService iinterface) throws Exception {
				final String importFile = directory + '/' + GlobalConstants.MAIN_PACKAGE + ".xml";
				if (!iinterface.isFile(importFile)) {
					appendStatus(GlobalConstants.MAIN_PACKAGE + ": Error. Not a file: "
							+ importFile);
					return;
				}

				final byte[] bytes = iinterface.readFileBytes(importFile);
				final String fileContents = new String(bytes, "UTF-8");
				SharedPreferencesUtil.importFromReader(
						Settings.getInstance(ImportExportSettings.this).getSharedPreferences(),
						new StringReader(fileContents));
				appendStatus(GlobalConstants.MAIN_PACKAGE + ": Imported");
			}

			@Override
			public void onException(Exception e) {
				LOG.e("performTask", e);
				appendStatus(GlobalConstants.MAIN_PACKAGE + ": " + e.getLocalizedMessage());
			}

		}.go();

		List<String> packages = new LinkedList<String>();
		packages.addAll(ModuleRegistry.getInstance(this).getAllModulePackages());
		packages.addAll(TransportRegistry.getInstance(this).getAllTransportPackages());

		for (final String pkg : packages) {
			new AsyncServiceTask<IFileReadModuleService>(new Intent(
					GlobalConstants.ACTION_BIND_FILEREAD), this) {

				@Override
				public IFileReadModuleService asInterface(IBinder iBinder) {
					return IFileReadModuleService.Stub.asInterface(iBinder);
				}

				@Override
				public void performTask(IFileReadModuleService iinterface) throws Exception {
					final String importFile = directory + '/' + pkg + ".xml";
					if (!iinterface.isFile(importFile)) {
						appendStatus(pkg + ": Error. Not a file: " + importFile);
						return;
					}

					final byte[] bytes = iinterface.readFileBytes(importFile);
					final String fileContents = new String(bytes, "UTF-8");

					final Intent intent = new Intent(GlobalConstants.ACTION_IMPORT_SETTINGS);
					intent.putExtra(GlobalConstants.EXTRA_CONTENT, fileContents);
					for (String receiver : Constants.COMPONENT_RECEIVERS) {
						intent.setClassName(pkg, pkg + '.' + receiver);
						sendBroadcast(intent);
					}
				}

				@Override
				public void onException(Exception e) {
					LOG.e("performTask", e);
					appendStatus(pkg + ": " + e.getLocalizedMessage());
				}

			}.go();
		}
	}

	private final void displayFileManagerInstallDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("OI File Manager, which is required to select a directory to import settings from, is not installed.");
		builder.setPositiveButton("Install from Play Store", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent install = new Intent(Intent.ACTION_VIEW, OIFM_MARKET_URI);
				if (mPackageManagerUtil.isIntentAvailable(install)) {
					startActivity(install);
				} else {
					Toast.makeText(ImportExportSettings.this, "Play Store not available",
							Toast.LENGTH_LONG).show();
				}
				dialog.dismiss();
			}
		});
		builder.setNeutralButton("Install from F-Droid", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(new Intent(Intent.ACTION_VIEW, OIFM_FDROID_URI));
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
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
