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

import java.util.HashSet;
import java.util.Set;

import org.projectmaxs.module.contactsread.commands.ContactLookup;
import org.projectmaxs.module.contactsread.commands.ContactMobile;
import org.projectmaxs.module.contactsread.commands.ContactName;
import org.projectmaxs.module.contactsread.commands.ContactNick;
import org.projectmaxs.module.contactsread.commands.ContactNum;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SupraCommand;

import android.content.Context;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	public ModuleService() {
		super(LOG, "maxs-module-contactsread", sCOMMANDS);
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.contactsread",      // Package of the Module
			"MAXS Module ContactsRead"                  // Name of the Module
			);
	// @formatter:on

	public static final SupraCommand[] sCOMMANDS;

	static {
		Set<SupraCommand> commands = new HashSet<SupraCommand>();

		SupraCommand.register(ContactLookup.class, commands);
		SupraCommand.register(ContactMobile.class, commands);
		SupraCommand.register(ContactName.class, commands);
		SupraCommand.register(ContactNick.class, commands);
		SupraCommand.register(ContactNum.class, commands);

		sCOMMANDS = commands.toArray(new SupraCommand[commands.size()]);
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}
}
