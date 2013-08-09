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

package org.projectmaxs.main.misc;

import android.content.Context;

public class ImportExportSettingsManager {

	private static ImportExportSettingsManager sManager;

	private final Context mContext;

	private ImportExportSettingsManager(Context context) {
		mContext = context;
	}

	public static synchronized ImportExportSettingsManager getInstance(Context context) {
		if (sManager == null) sManager = new ImportExportSettingsManager(context);
		return sManager;
	}

	public void exportToFile(String file, String content) {
		// TODO
		// if (file == null || content == null) return;
		// boolean saved = FileManager.saveToFile(file, content);
		// if (saved) {
		// ImportExportSettings.appendStatus("exported settings to " + file);
		// }
	}

}
