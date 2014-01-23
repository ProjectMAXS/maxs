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

package org.projectmaxs.module.contactsread.commands;

import java.util.Collection;
import java.util.Iterator;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.ContactUtil;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.ModuleConstants;
import org.projectmaxs.shared.module.SubCommand;

public abstract class AbstractContactCommand extends SubCommand {

	public AbstractContactCommand(String name) {
		super(ModuleConstants.CONTACT, name);
	}

	public AbstractContactCommand(String name, boolean isDefaultWithoutArguments,
			boolean isDefaultWithArguments) {
		super(ModuleConstants.CONTACT, name, isDefaultWithoutArguments, isDefaultWithArguments);
	}

	ContactUtil mContactUtil;

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		mContactUtil = ContactUtil.getInstance(service);

		return null;
	}

	static final Message processResult(Collection<Contact> contacts) {
		Iterator<Contact> it = contacts.iterator();
		if (!it.hasNext()) return new Message("No Contacts found");
		Message msg = new Message();
		while (it.hasNext()) {
			msg.add(it.next());
		}
		return msg;
	}
}
