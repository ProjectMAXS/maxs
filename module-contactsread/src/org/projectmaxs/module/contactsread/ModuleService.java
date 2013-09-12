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

package org.projectmaxs.module.contactsread;

import java.util.Collection;
import java.util.Iterator;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.Contact;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.ContactUtil;
import org.projectmaxs.shared.module.MAXSModuleIntentService;

import android.content.Context;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-contactsread");
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.contactsread",      // Package of the Module
			"contactsread",                             // Name of the Module (if omitted, last substring after '.' is used)
			new ModuleInformation.Command[] {        // Array of commands provided by the module
					new ModuleInformation.Command(
							"contacts",             // Command name
							"c",                    // Short command name
							null,                // Default subcommand without arguments
							"lookup",                    // Default subcommand with arguments
							new String[] { "lookup", "lname", "lnum", "lnick" }),  // Array of provided subcommands 
			});
	// @formatter:on

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}

	@Override
	public Message handleCommand(Command command) {
		Message msg;
		String subCmd = command.getSubCommand();
		String args = command.getArgs();
		if ("lookup".equals(subCmd)) {
			msg = lookup(args);
		}
		else if ("lname".equals(subCmd)) {
			msg = lookupByName(args);
		}
		else if ("lnum".equals(subCmd)) {
			msg = lookupByNumber(args);
		}
		else if ("lnick".equals(subCmd)) {
			msg = lookupByNickname(args);
		}
		else {
			msg = new Message("Unkown command");
		}
		return msg;
	}

	private Message lookup(String args) {

		Collection<Contact> contacts = ContactUtil.getInstance(this).lookupContacts(args);
		Iterator<Contact> it = contacts.iterator();
		if (!it.hasNext()) return new Message("No Contacts found");
		Message msg = new Message();
		while (it.hasNext()) {

		}
		return null;
	}

	private Message lookupByName(String args) {
		return null;
	}

	private Message lookupByNumber(String args) {
		return null;
	}

	private Message lookupByNickname(String args) {
		return null;
	}
}
