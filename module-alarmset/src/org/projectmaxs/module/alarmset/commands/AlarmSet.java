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

package org.projectmaxs.module.alarmset.commands;

import org.projectmaxs.module.alarmset.ModuleService;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SubCommand;

import android.annotation.TargetApi;
import android.content.Intent;
import android.provider.AlarmClock;

@TargetApi(19)
public class AlarmSet extends SubCommand {

	public AlarmSet() {
		super(ModuleService.ALARM, "set", false, true);
		setHelp("HH:mm [description]", "Set a new alarm");
		setRequiresArgument();
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service) {

		String time;
		String alarmDescription;
		int spaceIndex = arguments.indexOf(' ');
		if (spaceIndex == -1) {
			time = arguments;
			alarmDescription = null;
		} else {
			time = arguments.substring(0, spaceIndex);
			alarmDescription = arguments.substring(spaceIndex);
		}

		String[] splitedTime = time.split(":");
		if (splitedTime.length != 2) return new Message("Invalid time format: " + time, false);

		int hour = Integer.parseInt(splitedTime[0]);
		int minutes = Integer.parseInt(splitedTime[1]);

		if (alarmDescription == null) alarmDescription = "Created by MAXS";

		Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
		intent.putExtra(AlarmClock.EXTRA_MINUTES, minutes);
		intent.putExtra(AlarmClock.EXTRA_HOUR, hour);
		intent.putExtra(AlarmClock.EXTRA_MESSAGE, alarmDescription);
		intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		service.startActivity(intent);

		return new Message("Alarm set");
	}
}
