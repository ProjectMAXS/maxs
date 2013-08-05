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

import java.util.List;

import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.StatusInformation;
import org.projectmaxs.shared.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class MAXSStatusBroadcastReceiver extends BroadcastReceiver {

	private static final Log LOG = Log.getLog();

	@Override
	public void onReceive(Context context, Intent intent) {
		List<StatusInformation> infos = onReceiveReturnStatusInformation(context, intent);
		if (infos == null) {
			LOG.e("onReceive: infos was null");
			return;
		}
		if (infos.isEmpty()) {
			LOG.e("onReceive: infos is empty");
			return;
		}

		for (StatusInformation info : infos) {
			Intent replyIntent = new Intent(GlobalConstants.ACTION_UPDATE_STATUS);
			replyIntent.putExtra(GlobalConstants.EXTRA_CONTENT, info);
			context.startService(replyIntent);
		}
	}

	public abstract List<StatusInformation> onReceiveReturnStatusInformation(Context context, Intent intent);

}
