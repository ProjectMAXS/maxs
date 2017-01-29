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

package org.projectmaxs.module.notificationpolicyaccess.commands;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SubCommand;
import org.projectmaxs.shared.module.SupraCommand;

import android.app.NotificationManager;

public abstract class AbstractNotificationManagerCommand extends SubCommand {

	protected AbstractNotificationManagerCommand(SupraCommand supraCommand, String name) {
		super(supraCommand, name);
	}

	protected AbstractNotificationManagerCommand(SupraCommand supraCommand, String name,
			boolean isDefaultWithoutArguments) {
		super(supraCommand, name, isDefaultWithoutArguments);
	}

	@Override
	public final Message execute(String arguments, Command command,
			MAXSModuleIntentService service) {
		NotificationManager notificationManager = service
				.getSystemService(NotificationManager.class);

		if (!notificationManager.isNotificationPolicyAccessGranted()) {
			return new Message(
					"MAXS module-notificationpolicyaccess was not granted access to the policy. Use \"np request\" to open the settings screen on the device so that you can grant the access.");
		}

		return execute(arguments, command, service, notificationManager);
	}

	protected abstract Message execute(String arguments, Command command,
			MAXSModuleIntentService service, NotificationManager notificationManager);

	public static String interruptionFilterToString(int interruptionFilter) {
		switch (interruptionFilter) {
		case NotificationManager.INTERRUPTION_FILTER_ALARMS:
			return "alarms";
		case NotificationManager.INTERRUPTION_FILTER_ALL:
			return "all";
		case NotificationManager.INTERRUPTION_FILTER_NONE:
			return "none";
		case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
			return "priority";
		case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
			return "unknown";
		default:
			throw new IllegalStateException("Unknown interruption filter int: " + interruptionFilter);
		}
	}

	public static int interruptionFilterToInt(String interruptionFilter) {
		switch (interruptionFilter) {
		case "alarms":
			return NotificationManager.INTERRUPTION_FILTER_ALARMS;
		case "all":
			return NotificationManager.INTERRUPTION_FILTER_ALL;
		case "none":
			return NotificationManager.INTERRUPTION_FILTER_NONE;
		case "priority":
			return NotificationManager.INTERRUPTION_FILTER_PRIORITY;
		case "unknown":
			return NotificationManager.INTERRUPTION_FILTER_UNKNOWN;
		default:
			throw new IllegalStateException(
					"Unknown interruption filter type: " + interruptionFilter);
		}
	}
}
