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

import org.projectmaxs.main.database.CommandTable;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class PurgeOldCommandsService extends IntentService {

	private static final Log LOG = Log.getLog();

	public PurgeOldCommandsService() {
		super("PurgeOldCommandsService");
	}

	public static void init(Context context) {
		Intent intent = new Intent(context, PurgeOldCommandsService.class);
		PendingIntent operation = PendingIntent.getService(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, AlarmManager.INTERVAL_DAY,
				operation);
	}

	@Override
	protected void onHandleIntent(Intent i) {
		CommandTable commandTable = CommandTable.getInstance(this);

		LOG.d("onHandleIntent: Alarm intent received. Current entry count: "
				+ commandTable.getEntryCount());

		int[] oldCommandIds = commandTable.getOldEntries();
		if (oldCommandIds == null) {
			LOG.d("onHandleIntent: No old command ids found");
			return;
		}

		commandTable.purgeEntries(oldCommandIds);

		LOG.d("onHandleIntent: Deleted " + oldCommandIds.length
				+ " commands from table. New entry count: " + commandTable.getEntryCount()
				+ ". Broadcasting purge old commands intent.");

		Intent intent = new Intent(GlobalConstants.ACTION_PURGE_OLD_COMMANDS);
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, oldCommandIds);
		sendBroadcast(intent);
	}
}
