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

package org.projectmaxs.module.misc.commands;

import java.util.Iterator;
import java.util.List;

import org.projectmaxs.module.misc.ModuleService;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SubCommand;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.os.Debug.MemoryInfo;

public class SysinfoMaxs extends SubCommand {

	public SysinfoMaxs() {
		super(ModuleService.SYSINFO, "maxs");
		setHelp(ArgType.NONE, "Show information about the system usage of MAXS components");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {

		final ActivityManager activityManager = (ActivityManager) service
				.getSystemService(Context.ACTIVITY_SERVICE);

		// retrieve the running services (incl. their PIDs)
		List<RunningServiceInfo> rsis = activityManager.getRunningServices(Integer.MAX_VALUE);
		Iterator<RunningServiceInfo> it = rsis.iterator();
		// filter out the services not related to MAXS
		while (it.hasNext()) {
			RunningServiceInfo rsi = it.next();
			if (!rsi.service.getPackageName().startsWith(GlobalConstants.PACKAGE)) {
				it.remove();
			}
		}

		// retrieve the MemoryInfo of the running MAXS services
		final int[] pids = new int[rsis.size()];
		for (int i = 0; i < rsis.size(); i++) {
			pids[i] = rsis.get(i).pid;
		}
		final MemoryInfo[] meminfo = activityManager.getProcessMemoryInfo(pids);

		// add the information to the message text
		final Text text = new Text();
		text.addBoldNL("MAXS Information");
		for (int i = 0; i < rsis.size(); i++) {
			RunningServiceInfo rsi = rsis.get(i);
			text.addBoldNL("Package: " + rsi.service.getPackageName());
			text.addNL("Service: " + rsi.service.getShortClassName());
			text.addNL("Client Count: " + rsi.clientCount);
			text.addNL("PID: " + rsi.pid);
			MemoryInfo mi = meminfo[i];
			text.addNL("MemoryInfo of PID " + pids[i]);
			text.addNL("Native Private Dirty: " + mi.nativePrivateDirty);
			text.addNL("Dalvik Private Dirty: " + mi.dalvikPrivateDirty);
			text.addNL("Other Private Dirty: " + mi.otherPrivateDirty);
			text.addNL("Total Private Dirty: " + mi.getTotalPrivateDirty());
			text.addNL("Total PSS: " + mi.getTotalPss());
			text.addNL("Dalvik PSS: " + mi.dalvikPss);
		}

		return new Message(text);
	}
}
