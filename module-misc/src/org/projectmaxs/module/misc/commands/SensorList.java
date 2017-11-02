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

import java.util.List;

import org.projectmaxs.module.misc.ModuleService;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SubCommand;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

public class SensorList extends SubCommand {

	public SensorList() {
		super(ModuleService.SENSOR, "list", true);
		setHelp(ArgType.NONE, "List the available sensors");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		SensorManager sensorManager = (SensorManager) service
				.getSystemService(Context.SENSOR_SERVICE);

		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);

		Message message = new Message("Available sensors");
		for (Sensor sensor : sensors) {
			Text sensorText = new Text(sensor.toString());
			message.add(sensorText);
		}

		return message;
	}
}
