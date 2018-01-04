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

import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;

public class NotificationInterruptionFilterNone
		extends AbstractNotificationInterruptionFilterSetCommand {

	public NotificationInterruptionFilterNone() {
		super("none");
		setHelp(ArgType.NONE,
				"Set the notifification interruption filter to 'none', which means that all notifications are suppressed and vibrations are muted");
	}

}
