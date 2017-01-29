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

import org.projectmaxs.module.notificationpolicyaccess.ModuleService;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SubCommand;

import android.app.NotificationManager;
import android.content.Intent;
import android.provider.Settings;

public class NotificationPolicyRequest
		extends SubCommand {

	public NotificationPolicyRequest() {
		super(ModuleService.NOTIFICATION_POLICY, "request");
		setHelp(ArgType.NONE,
				"Request access to the notification policy");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service) {
		NotificationManager notificationManager = service
				.getSystemService(NotificationManager.class);

		if (notificationManager.isNotificationPolicyAccessGranted()) {
			return new Message("Notification policy access was already granted");
		}

		Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		service.startActivity(intent);
		return new Message(
				"Notification Policy Settings opened on device. Please grant the access now on the device.");
	}
}
