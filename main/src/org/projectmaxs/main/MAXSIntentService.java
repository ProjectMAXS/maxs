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

import org.projectmaxs.main.activities.ImportExportSettings;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;

import android.app.IntentService;
import android.content.Intent;

public class MAXSIntentService extends IntentService {

	private static final Log LOG = Log.getLog();

	public MAXSIntentService() {
		super("MAXSService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		LOG.d("handleIntent() Action: " + action);
		if (action.equals(GlobalConstants.ACTION_EXPORT_TO_FILE)) {
			final String file = intent.getStringExtra(GlobalConstants.EXTRA_FILE);
			final String content = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			// use the application context here, otherwise we will get leaked
			// serviceConnection errors.
			ImportExportSettings.tryToExport(file, content.getBytes(), getApplicationContext());
		}
		else if (action.equals(GlobalConstants.ACTION_IMPORT_EXPORT_STATUS)) {
			String status = intent.getStringExtra(GlobalConstants.EXTRA_COMMAND);
			if (status == null) return;
			ImportExportSettings.appendStatus(status);
		}
		else {
			// TODO throw new IllegalStateException();
		}
	}
}
