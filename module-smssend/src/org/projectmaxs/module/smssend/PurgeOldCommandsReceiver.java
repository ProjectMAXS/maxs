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

package org.projectmaxs.module.smssend;

import org.projectmaxs.module.smssend.database.SMSTable;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.module.MAXSPurgeOldCommandsReceiver;

import android.content.Context;

public class PurgeOldCommandsReceiver extends MAXSPurgeOldCommandsReceiver {

	private static final Log LOG = Log.getLog();

	@Override
	public void purgeOldCommands(int[] commandIds, Context context) {
		LOG.d("purgeOldCommands: Received " + commandIds.length
				+ " ID(s) that could get deleted from SMSTable");
		SMSTable smsTable = SMSTable.getInstance(context);
		smsTable.purgeEntries(commandIds);
	}

}
