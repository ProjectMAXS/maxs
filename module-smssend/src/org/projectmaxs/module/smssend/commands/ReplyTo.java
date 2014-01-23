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

package org.projectmaxs.module.smssend.commands;

import org.projectmaxs.module.smssend.ModuleService;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.messagecontent.ContactNumber;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.RecentContact;
import org.projectmaxs.shared.module.ContactUtil;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.RecentContactUtil;

public class ReplyTo extends AbstractSmsSendCommand {

	public ReplyTo() {
		super(ModuleService.sREPLY, "to", false, true);
		setHelp(ArgType.NONE, "Send a SMS message to the recent contact");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		super.execute(arguments, command, service);

		Contact contact = null;

		RecentContact recentContact = RecentContactUtil.getRecentContact(mService);
		if (recentContact == null) return new Message("No recent contact");
		if (recentContact.mContact != null) {
			contact = recentContact.mContact;
		} else {
			contact = new Contact();
		}
		if (ContactNumber.isNumber(recentContact.mContactInfo)) {
			contact.addNumber(recentContact.mContactInfo);
		} else {
			// If the contact info is not a number, e.g. because we received
			// an SMS with a company name as sender, then try to fill in the
			// missing information
			ContactUtil.getInstance(mService).lookupContactNumbersFor(contact);
			if (contact.hasNumbers()) {
				return new Message("No number for contact");
			}
		}
		String text = command.getArgs();
		String receiver = contact.getBestNumber(ContactNumber.NumberType.MOBILE).getNumber();

		return sendSms(receiver, text, command.getId(), contact);
	}
}
