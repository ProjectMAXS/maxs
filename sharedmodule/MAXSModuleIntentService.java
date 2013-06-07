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

package org.projectmaxs.sharedmodule;

import org.projectmaxs.shared.Command;
import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.Message;
import org.projectmaxs.shared.UserMessage;
import org.projectmaxs.shared.util.Log;
import org.projectmaxs.shared.util.Log.LogSettings;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

public abstract class MAXSModuleIntentService extends IntentService {
	private static Log sLog;

	public MAXSModuleIntentService(String name) {
		super(name);
		sLog = Log.getLog(name);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sLog.initialize(new LogSettings() {
			// TODO add real log settings
			@Override
			public boolean debugLog() {
				return true;
			}
		});
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		sLog.d("onHandleIntent");
		Command command = intent.getParcelableExtra(GlobalConstants.EXTRA_COMMAND);

		Message msg = handleCommand(command);
		if (msg == null) return;

		Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_USER_MESSAGE);
		replyIntent.putExtra(GlobalConstants.EXTRA_USER_MESSAGE, new UserMessage(msg));
		startService(replyIntent);
	}

	public abstract Message handleCommand(Command command);

}
