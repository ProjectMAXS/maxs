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

package org.projectmaxs.shared.mainmodule;

import java.util.ArrayList;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.StatusInformation;
import org.projectmaxs.shared.global.util.Log;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class MAXSStatusUtil {

	private static final Log LOG = Log.getLog();

	public static void maybeUpdateStatus(Context context, StatusInformation statusInformation) {
		ArrayList<StatusInformation> statusInformations = new ArrayList<>(1);
		maybeUpdateStatus(context, statusInformations);
	}

	public static void maybeUpdateStatus(Context context,
			ArrayList<StatusInformation> statusInformations) {
		// Be done here if there are now new status information to report
		if (statusInformations.size() == 0) return;

		Intent replyIntent = new Intent();
		replyIntent.setClassName(GlobalConstants.MAIN_PACKAGE,
				MainModuleConstants.MAIN_MODULE_SERVICE);
		replyIntent.setAction(GlobalConstants.ACTION_UPDATE_STATUS);
		replyIntent.putParcelableArrayListExtra(GlobalConstants.EXTRA_CONTENT, statusInformations);
		ComponentName componentName = context.startService(replyIntent);
		if (componentName == null) {
			LOG.w("Could not find component to update status");
		}
	}
}
