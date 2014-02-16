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

package org.projectmaxs.module.phonestateread;

import org.projectmaxs.shared.global.GlobalConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartStopReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Intent i = new Intent(context, PhoneStateService.class);
		if (GlobalConstants.ACTION_SERVICE_STARTED.equals(action)) {
			i.setAction(Constants.START_PHONESTATE_SERVICE);
		} else if (GlobalConstants.ACTION_SERVICE_STOPED.equals(action)) {
			i.setAction(Constants.STOP_PHONESTATE_SERVICE);
		} else {
			throw new IllegalArgumentException();
		}
		context.startService(i);
	}

}
