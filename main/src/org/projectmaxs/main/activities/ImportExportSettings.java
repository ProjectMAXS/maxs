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

import java.io.File;

import org.projectmaxs.main.R;
import org.projectmaxs.main.Settings;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.main.util.FileManager;
import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.util.Log;
import org.projectmaxs.shared.util.SharedPreferencesUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ImportExportSettings extends Activity {

	private static final Log LOG = Log.getLog();

	EditText mImportDirectory;
	static TextView sImportExportStatus;

	public static void appendStatus(String string) {
		if (sImportExportStatus == null) {
			LOG.d("appendStatus: sImportExportStatus was null");
			return;
		}
		sImportExportStatus.append(string + "\n");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.importexportsettings);
		mImportDirectory = (EditText) findViewById(R.id.importDirectory);
		sImportExportStatus = (TextView) findViewById(R.id.textImportExportStatus);
	}

	public void exportAll(View view) {
		sImportExportStatus.setText("");

		File mainOutFile = null;
		try {
			File exportDir = FileManager.getInstance(this).getTimestampedSettingsExportDir();
			appendStatus("exportAll: set export directory to " + exportDir.getCanonicalPath());
			mainOutFile = new File(exportDir, Constants.MAIN_PACKAGE + ".xml");
			SharedPreferencesUtil.exportToFile(Settings.getInstance(this).getSharedPreferences(), mainOutFile,
					Settings.DO_NOT_EXPORT);
		} catch (Exception e) {
			LOG.e("exportAll: exception", e);
			appendStatus("exportAll: exception " + e.getMessage());
		}
		appendStatus("exportAll: exported main settings to " + mainOutFile.toString());

		final Intent intent = new Intent(GlobalConstants.ACTION_EXPORT_SETTINGS);
		intent.putExtra(GlobalConstants.EXTRA_FILE, mainOutFile.getAbsolutePath());
		sendBroadcast(intent);
	}
}
