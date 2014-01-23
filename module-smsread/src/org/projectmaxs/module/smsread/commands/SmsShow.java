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

package org.projectmaxs.module.smsread.commands;

import java.util.List;

import org.projectmaxs.module.smsread.SmsUtil;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Sms;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.ModuleConstants;
import org.projectmaxs.shared.module.SubCommand;

public class SmsShow extends SubCommand {

	public SmsShow() {
		super(ModuleConstants.SMS, "show");
		setHelp("<count>", "Show the last 5 or $count SMS messages");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		if (arguments != null && !SharedStringUtil.isInteger(arguments)) {
			// TODO show last messages by string (e.g. contact name or number)
			return new Message("not implemented yet");
		}

		int count;
		if (arguments == null) {
			count = 5;
		} else {
			count = Integer.parseInt(arguments);
		}
		Message msg = new Message();
		msg.add(Text.createBoldNL("Last " + count + " SMS messages"));
		List<Sms> sms = SmsUtil.getOrderedSMS(null, null, count, service);
		msg.addAll(sms);

		return msg;
	}

}
