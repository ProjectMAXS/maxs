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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import org.projectmaxs.shared.global.util.DialogUtil;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.PackageManagerUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import eu.geekplace.iesp.ImportExportSharedPreferences;

public class ImportExportSettings extends Activity {

	private static final Handler HANDLER = new Handler();
	private static final Log LOG = Log.getLog();
	private static final String PICK_DIRECTORY_INTENT = "org.openintents.action.PICK_DIRECTORY";
	private static final String OIFM_PACKAGE = "org.openintents.filemanager";
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
			DialogUtil
					.displayPackageInstallDialog(
							"The filewrite module,  which is required to read the data,  is not installed.",
							GlobalConstants.FILEWRITE_MODULE_PACKAGE, this);
			return;
		}

		File exportDir = FileManager.getTimestampedSettingsExportDir();
		appendStatus("set export directory to " + exportDir.getAbsolutePath());
		File mainOutFile = new File(exportDir, Constants.MAIN_PACKAGE + ".xml");
		final String file = mainOutFile.getAbsolutePath();
		Writer writer = new CharArrayWriter();
		try {
			ImportExportSharedPreferences.export(Settings.getInstance(this).getSharedPreferences(),
					writer, null);
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
			DialogUtil
					.displayPackageInstallDialog(
							"OI File Manager, which is required to select a directory to import settings from, is not installed.",
							OIFM_PACKAGE, this);
			return;
		}
		if (!mPackageManagerUtil.isPackageInstalled(GlobalConstants.FILEREAD_MODULE_PACKAGE)) {
			DialogUtil.displayPackageInstallDialog(
					"The fileread module,  which is required to read the data,  is not installed.",
					GlobalConstants.FILEREAD_MODULE_PACKAGE, this);
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
		Intent intent = new Intent(GlobalConstants.ACTION_BIND_FILEREAD);
		intent.setClassName(GlobalConstants.FILEREAD_MODULE_PACKAGE,
				GlobalConstants.FILEREAD_SERVICE);
		AsyncServiceTask.builder(this, intent,
				new AsyncServiceTask.IBinderAsInterface<IFileReadModuleService>() {
					@Override
					public IFileReadModuleService asInterface(IBinder iBinder) {
						return IFileReadModuleService.Stub.asInterface(iBinder);
					}
				},
				new AsyncServiceTask.PerformAsyncTask<IFileReadModuleService, Exception>() {
					@Override
					public void performTask(IFileReadModuleService iinterface)
							throws Exception {
						final String importFile = directory + '/' + GlobalConstants.MAIN_PACKAGE + ".xml";
						if (!iinterface.isFile(importFile)) {
							appendStatus(GlobalConstants.MAIN_PACKAGE + ": Error. Not a file: "
									+ importFile);
							return;
						}

						final byte[] bytes = iinterface.readFileBytes(importFile);
						final String fileContents = new String(bytes, "UTF-8");
						ImportExportSharedPreferences.importFromReader(
								Settings.getInstance(ImportExportSettings.this).getSharedPreferences(),
								new StringReader(fileContents));
						appendStatus(GlobalConstants.MAIN_PACKAGE + ": Imported");
					}
				},
				Exception.class)
		.withExceptionHandler(new AsyncServiceTask.ExceptionHandler<Exception>() {
					@Override
					public void onException(Exception e, Exception specificExcepiton,
							RemoteException optionalRemoteException) {
						LOG.e("performTask", e);
						appendStatus(GlobalConstants.MAIN_PACKAGE + ": " + e.getLocalizedMessage());
					}
		})
		.build()
		.go();

		List<String> packages = new LinkedList<String>();
		packages.addAll(ModuleRegistry.getInstance(this).getAllModulePackages());
		packages.addAll(TransportRegistry.getInstance(this).getAllTransportPackages());

		// TODO: Does this executor stop if it goes out of scope and no more threads are
		// running/pending?
		Executor singleThreadExecutor = new ThreadPoolExecutor(0, 1, 100L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());

		for (final String pkg : packages) {
			AsyncServiceTask.builder(this, intent,
					new AsyncServiceTask.IBinderAsInterface<IFileReadModuleService>() {
						@Override
						public IFileReadModuleService asInterface(IBinder iBinder) {
							return IFileReadModuleService.Stub.asInterface(iBinder);
						}
					},
					new AsyncServiceTask.PerformAsyncTask<IFileReadModuleService, UnsupportedEncodingException>() {
						@Override
						public void performTask(IFileReadModuleService iinterface)
								throws RemoteException, UnsupportedEncodingException {
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
					},
					UnsupportedEncodingException.class)
					.withExceptionHandler(new AsyncServiceTask.ExceptionHandler<UnsupportedEncodingException>() {
						@Override
						public void onException(Exception e,
								UnsupportedEncodingException specificExcepiton,
								RemoteException optionalRemoteException) {
							LOG.e("performTask", e);
							appendStatus(pkg + ": " + e.getLocalizedMessage());
				}})
			.withExecutor(singleThreadExecutor)
			.build()
			.go();
		}
	}

	public static void tryToExport(final String file, final byte[] bytes, Context context) {
		Intent intent = new Intent(GlobalConstants.ACTION_BIND_FILEWRITE);
		intent.setClassName(GlobalConstants.FILEWRITE_MODULE_PACKAGE,
				GlobalConstants.FILEWRITE_SERVICE);
		AsyncServiceTask.builder(context, intent,
				new AsyncServiceTask.IBinderAsInterface<IFileWriteModuleService>() {
					@Override
					public IFileWriteModuleService asInterface(IBinder iBinder) {
						return IFileWriteModuleService.Stub.asInterface(iBinder);
					}
				},
				new AsyncServiceTask.PerformAsyncTask<IFileWriteModuleService, RuntimeException>() {
					@Override
					public void performTask(IFileWriteModuleService iinterface)
							throws RuntimeException {
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
				},
				RuntimeException.class)
		.build()
		.go();
	}
}
