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

package org.projectmaxs.module.contacts;

import java.util.List;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.Contact;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.mainmodule.aidl.IContactsModuleService;
import org.projectmaxs.shared.module.MAXSModuleIntentService;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-contacts");
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.contacts",      // Package of the Module
			"contacts",                             // Name of the Module (if omitted, last substring after '.' is used)
			new ModuleInformation.Command[] {        // Array of commands provided by the module
					new ModuleInformation.Command(
							"contacts",             // Command name
							"c",                    // Short command name
							"lookup",                // Default subcommand without arguments
							null,                    // Default subcommand with arguments
							new String[] { "status" }),  // Array of provided subcommands 
			});
	// @formatter:on

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public Message handleCommand(Command command) {
		Message msg;
		if (command.getSubCommand().equals("lookup")) {
			msg = new Message("TODO contact lookup");
		}
		else {
			msg = new Message("Unkown command");
		}
		return msg;
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}

	private List<Contact> lookupContact(String lookupInfo) {
		return null;
	}

	private List<Contact> lookupContactFromNumber(String number) {
		return null;
	}

	private final IContactsModuleService.Stub mBinder = new IContactsModuleService.Stub() {

		@Override
		public List<Contact> lookupContact(String lookupInfo) {
			return ModuleService.this.lookupContact(lookupInfo);
		}

		@Override
		public List<Contact> lookupContactFromNumber(String number) {
			return ModuleService.this.lookupContactFromNumber(number);
		}

		@Override
		public Contact lookupOneContactFromNumber(String number) {
			List<Contact> contacts = ModuleService.this.lookupContactFromNumber(number);
			if (contacts == null) return null;
			return contacts.get(0);
		}
	};
}
