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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.projectmaxs.main.CommandInformation.CommandClashException;
import org.projectmaxs.main.database.ModuleRegistryTable;
import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.shared.util.Log;

import android.content.Context;
import android.content.Intent;

public class ModuleRegistry {

	private static ModuleRegistry sCommandRegistry;
	private static final Log LOG = Log.getLog();

	public static synchronized ModuleRegistry getInstance(Context context) {
		if (sCommandRegistry == null) sCommandRegistry = new ModuleRegistry(context);
		return sCommandRegistry;
	}

	private final Map<String, CommandInformation> mCommands = new HashMap<String, CommandInformation>();

	/**
	 * Maps a package to a set of all commands the package provides.
	 */
	private final Map<String, Set<String>> mPackageCommands = new HashMap<String, Set<String>>();

	/**
	 * Maps a short command to a command
	 */
	private final Map<String, String> mShortCommandMap = new HashMap<String, String>();

	/**
	 * Maps a package to a set of all short commands the package created.
	 * Although a Command can only have one short command, it is possible for a
	 * package to create multiple short command. And that multiple packages
	 * create multiple short commands for a single command (but that would be
	 * considered bad practice).
	 */
	private final Map<String, Set<String>> mPackageShortCommands = new HashMap<String, Set<String>>();

	private Context mContext;
	private ModuleRegistryTable mModuleRegistryTable;
	private boolean mLoadedFromDatabase = false;

	private ModuleRegistry(Context context) {
		mContext = context;
		mModuleRegistryTable = ModuleRegistryTable.getInstance(context);
	}

	protected synchronized void loadFromDatabase() {
		if (!mLoadedFromDatabase) {
			Iterator<ModuleInformation> it = mModuleRegistryTable.getAll().iterator();
			while (it.hasNext())
				add(it.next());
			mLoadedFromDatabase = true;
		}
	}

	protected void challengeAllModulesToRegister() {
		mContext.sendBroadcast(new Intent(GlobalConstants.ACTION_REGISTER));
	}

	protected synchronized Collection<Set<String>> getAllModules() {
		return Collections.unmodifiableCollection(mPackageCommands.values());
	}

	protected synchronized CommandInformation get(String command) {
		command = mShortCommandMap.containsKey(command) ? mShortCommandMap.get(command) : command;
		return mCommands.get(command);
	}

	public synchronized void unregisterModule(String modulePackage) {
		if (!mModuleRegistryTable.containsModule(modulePackage)) return;
		remove(modulePackage);
		mModuleRegistryTable.deleteModuleInformation(modulePackage);
	}

	protected synchronized void registerModule(ModuleInformation moduleInformation) {
		// first remove all traces of the module
		remove(moduleInformation.getModulePackage());
		add(moduleInformation);
		mModuleRegistryTable.insertOrReplace(moduleInformation);
	}

	private void add(ModuleInformation moduleInformation) {
		String modulePackage = moduleInformation.getModulePackage();
		Set<ModuleInformation.Command> cmds = moduleInformation.getCommands();
		Set<String> packageCommands = new HashSet<String>();
		Set<String> packageShortCommands = new HashSet<String>();
		for (ModuleInformation.Command c : cmds) {
			String command = c.getCommand();
			CommandInformation ci = mCommands.get(command);
			if (ci == null) {
				ci = new CommandInformation(command);
				mCommands.put(command, ci);
			}
			try {
				ci.addSubAndDefCommands(c, modulePackage);
			} catch (CommandClashException e) {
				throw new IllegalStateException(e); // TODO
			}

			String shortCommand = c.getShortCommand();
			mShortCommandMap.put(shortCommand, command);
			packageShortCommands.add(shortCommand);

			packageCommands.add(command);
		}
		mPackageCommands.put(modulePackage, packageCommands);
		mPackageShortCommands.put(modulePackage, packageShortCommands);
	}

	private void remove(String modulePackage) {
		Iterator<CommandInformation> it = mCommands.values().iterator();
		while (it.hasNext()) {
			CommandInformation ci = it.next();
			boolean commandIsOrphan = ci.removeAllSubCommandsForPackage(modulePackage);
			// TODO this hopefully removes the command from the HashMap
			if (commandIsOrphan) it.remove();
		}

		Set<String> moduleAliases = mPackageShortCommands.get(modulePackage);
		if (moduleAliases != null) {
			Iterator<String> it2 = moduleAliases.iterator();
			while (it2.hasNext()) {
				String alias = it2.next();
				mShortCommandMap.remove(alias);
			}
			mPackageShortCommands.remove(modulePackage);
		}

		mPackageCommands.remove(modulePackage);
	}
}
