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

package org.projectmaxs.main;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.projectmaxs.main.CommandInformation.CommandClashException;
import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.ModuleInformation;

import android.content.Context;
import android.content.Intent;

public class ModuleRegistry {

	private static ModuleRegistry sCommandRegistry;

	protected static synchronized ModuleRegistry getInstance(Context context) {
		if (sCommandRegistry == null) sCommandRegistry = new ModuleRegistry(context);
		return sCommandRegistry;
	}

	private final Map<String, CommandInformation> mCommands = new HashMap<String, CommandInformation>();
	private Context mContext;

	private ModuleRegistry(Context context) {
		mContext = context;
	}

	protected void onStartService() {
		// let's assume that if the size is zero we have to do an initial
		// challenge
		if (mCommands.size() == 0) {
			// clear commands before challenging the modules to register
			mCommands.clear();
			mContext.sendBroadcast(new Intent(GlobalConstants.ACTION_REGISTER));
		}
	}

	protected CommandInformation get(String command) {
		return mCommands.get(command);
	}

	protected void registerModule(ModuleInformation moduleInformation) {
		String modulePackage = moduleInformation.getModulePackage();
		Set<ModuleInformation.Command> cmds = moduleInformation.getCommands();
		synchronized (mCommands) {
			for (ModuleInformation.Command c : cmds) {
				String cStr = c.getCommand();
				CommandInformation ci = mCommands.get(cStr);
				if (ci == null) {
					ci = new CommandInformation(cStr);
					mCommands.put(cStr, ci);
				}
				try {
					ci.addSubAndDefCommands(c, modulePackage);
				} catch (CommandClashException e) {
					throw new IllegalStateException(e); // TODO
				}
			}

		}
	}
}
