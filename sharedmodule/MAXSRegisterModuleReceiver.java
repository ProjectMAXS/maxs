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

import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.shared.util.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class MAXSRegisterModuleReceiver extends BroadcastReceiver {
	private final Log mLog;
	private final ModuleInformation mModuleInformation;

	public MAXSRegisterModuleReceiver(Log log, ModuleInformation moduleInformation) {
		mLog = log;
		mModuleInformation = moduleInformation;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		initLog(context);
		mLog.d("RegisterModuleReceiver");
		Intent replyIntent = new Intent(GlobalConstants.ACTION_REGISTER_MODULE);
		replyIntent.putExtra(GlobalConstants.EXTRA_MODULE_INFORMATION, mModuleInformation);
		context.startService(replyIntent);
	}

	public abstract void initLog(Context ctx);

}
