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

import java.util.Collection;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.messagecontent.ContactNumber;
import org.projectmaxs.shared.global.messagecontent.FormatedText;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.ContactUtil;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.ModuleConstants;
import org.projectmaxs.shared.module.RecentContactUtil;

public class SmsSend extends AbstractSmsSendCommand {

	public SmsSend() {
		super(ModuleConstants.SMS, "send", false, true);
		setHelp("<recipient info>␣␣<sms content>",
				"Send a sms. The contact needs to be seperated from the sms body with two spaces.");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		super.execute(arguments, command, service);

		Contact contact = null;
		String receiver = null;
		String[] argsSplit = command.getArgs().split("  ", 2);
		if (ContactNumber.isNumber(argsSplit[0])) {
			contact = ContactUtil.getInstance(mService).contactByNumber(argsSplit[0]);
			receiver = argsSplit[0];
		} else {

			Collection<Contact> contacts = ContactUtil.getInstance(mService).lookupContacts(
					argsSplit[0]);
			if (contacts == null) {
				return new Message("Contacts module (MAXS module contactsread) not installed?");
			} else if (contacts.size() > 1) {
				if (mSettings.useBestContactEnabled())
					contact = ContactUtil.getOnlyContactWithNumber(contacts);
				if (contact == null) return new Message("Many matching contacts found");
			} else if (contacts.size() == 0) {
				Text failureText = new Text("No matching contact for '" + argsSplit[0] + "' found.");
				int spaceCount = SharedStringUtil.countMatches(argsSplit[0], ' ');
				if (spaceCount > 2)
					failureText
							.add(FormatedText
									.from("Did you forget to seperate the name from the content with two spaces (sms␣send␣<name>␣␣<content>)? "));
				return new Message(failureText);
			} else {
				contact = contacts.iterator().next();
			}
			receiver = contact.getBestNumber(ContactNumber.NumberType.MOBILE).getNumber();
		}
		String text = argsSplit[1];
		RecentContactUtil.setRecentContact(receiver, contact, mService);

		return sendSms(receiver, text, command.getId(), contact);
	}
}
