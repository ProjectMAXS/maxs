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

package org.projectmaxs.module.fileread;

import java.io.File;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;

import android.app.IntentService;
import android.content.Intent;

public class SetCWDService extends IntentService {

	private static final Log LOG = Log.getLog();

	public SetCWDService() {
		super("SetCWDService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String cwdPath = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
		if (cwdPath == null) {
			LOG.e("onHandleIntent: cwdPath is null");
			return;
		}
		File cwd = new File(cwdPath);
		if (!cwd.isDirectory()) {
			LOG.e("onHandleIntent: not a directory cwd=" + cwd.getAbsolutePath());
			return;
		}

		Settings.getInstance(this).setCwd(cwd);
	}

}
