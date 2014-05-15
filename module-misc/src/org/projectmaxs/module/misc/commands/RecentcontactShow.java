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

import org.projectmaxs.module.misc.ModuleService;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.RecentContact;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.RecentContactUtil;
import org.projectmaxs.shared.module.SubCommand;

public class RecentcontactShow extends SubCommand {

	public RecentcontactShow() {
		super(ModuleService.RECENT_CONTACT, "show", true);
		setHelp(ArgType.NONE, "Show the recent contact");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		RecentContact recentContact = RecentContactUtil.getRecentContact(service);
		Element element;
		if (recentContact != null) {
			element = new Element("recentContact", "true", "Recent Contact");
			element.addChildElement(new Element("contactInfo", recentContact.mContactInfo,
					recentContact.mContactInfo));
			if (recentContact.mContact != null) {
				element.addChildElement(new Element("contactDisplayName", recentContact.mContact
						.getDisplayName(), "Name: " + recentContact.mContact.getDisplayName()));
				// Don't expose the lookup key to the user
				element.addChildElement(new Element("contactLookupId", recentContact.mContact
						.getLookupKey()));
			}
		} else {
			element = new Element("recentContact", "false", "No Recent Contact");
		}

		return new Message(element);
	}

}
