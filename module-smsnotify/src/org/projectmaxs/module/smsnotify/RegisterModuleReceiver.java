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

package org.projectmaxs.module.smsnotify;

import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.shared.util.Log;
import org.projectmaxs.shared.util.Log.LogSettings;
import org.projectmaxs.sharedmodule.MAXSRegisterModuleReceiver;

import android.content.Context;

public class RegisterModuleReceiver extends MAXSRegisterModuleReceiver {
	private static Log sLog = Log.getLog();

	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.smsnotify", new ModuleInformation.Command[0]);

	public RegisterModuleReceiver() {
		super(sLog, sMODULE_INFORMATION);
	}

	@Override
	public void initLog(Context ctx) {
		sLog.initialize(new LogSettings() {
			// TODO add real log settings
			@Override
			public boolean debugLog() {
				return true;
			}
		});
	}
}
