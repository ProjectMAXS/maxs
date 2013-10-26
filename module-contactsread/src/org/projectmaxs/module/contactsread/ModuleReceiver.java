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

import java.util.List;

import org.projectmaxs.shared.global.messagecontent.CommandHelp;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.module.MAXSModuleReceiver;

import android.content.Context;
import android.content.SharedPreferences;

public class ModuleReceiver extends MAXSModuleReceiver {
	private final static Log LOG = Log.getLog();

	public ModuleReceiver() {
		super(LOG, ModuleService.sMODULE_INFORMATION);
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}

	@Override
	public SharedPreferences getSharedPreferences(Context context) {
		return Settings.getInstance(context).getSharedPreferences();
	}

	@Override
	public void addHelp(List<CommandHelp> help, Context context) {
		help.add(new CommandHelp("contact", "lookup", ArgType.CONTACT_INFO, "Lookup a contact"));
		help.add(new CommandHelp("contact", "lname", ArgType.CONTACT_NAME, "Lookup by name"));
		help.add(new CommandHelp("contact", "lnum", ArgType.NUMBER, "Lookup by number"));
		help.add(new CommandHelp("contact", "lnick", ArgType.CONTACT_NICKNAME, "Lookup by nickname"));
		help.add(new CommandHelp("contact", "mobile", ArgType.NUMBER, "Lookup my mobile number"));
	}
}
