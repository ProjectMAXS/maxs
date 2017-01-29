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

import android.app.NotificationManager;
import android.app.NotificationManager.Policy;

public class NotificationPolicyShow
		extends AbstractNotificationManagerCommand {

	public NotificationPolicyShow() {
		super(ModuleService.NOTIFICATION_POLICY, "show");
		setHelp(ArgType.NONE,
				"Show the current notification policy");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service,
			NotificationManager notificationManager) {

		Policy policy = notificationManager.getNotificationPolicy();
		/*
		String priorityCategories = Policy.priorityCategoriesToString(policy.priorityCategories);
		String priorityCallSenders = Policy.prioritySendersToString(policy.priorityCallSenders);
		String priorityMessageSenders = Policy
				.prioritySendersToString(policy.priorityMessageSenders);
		*/

		return new Message("Notification manager policy: " + policy + '.');

	}
}
