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

package org.projectmaxs.shared.module;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.projectmaxs.shared.global.messagecontent.CommandHelp;
import org.projectmaxs.shared.mainmodule.ModuleInformation;

import android.content.Context;

public class SupraCommand {

	private final String mCommand;
	private final String mShortCommand;

	private final Map<String, SubCommand> mSubCommands = new HashMap<String, SubCommand>();

	private SubCommand mDefaultWithArguments;
	private SubCommand mDefaultWithoutArguments;

	public SupraCommand(String command) {
		this(command, null);
	}

	public SupraCommand(String command, String shortCommand) {
		mCommand = command;
		mShortCommand = shortCommand;
	}

	public String getCommand() {
		return mCommand;
	}

	public String getShortCommand() {
		return mShortCommand;
	}

	/**
	 * Check if a sub command of the given name exists
	 * 
	 * @param name
	 * @return the SubCommand or null of none found
	 */
	public SubCommand getSubCommand(String name) {
		return mSubCommands.get(name);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		if (this == o) return true;

		SupraCommand other = (SupraCommand) o;
		if (other.hashCode() == hashCode()) return true;
		return false;
	}

	@Override
	public int hashCode() {
		int hash = mCommand.hashCode();
		if (mShortCommand != null) hash += 31 * mShortCommand.hashCode();
		return hash;
	}

	public void addTo(ModuleInformation moduleInformation, Context context) {
		ModuleInformation.Command command = new ModuleInformation.Command(mCommand, mShortCommand,
				mDefaultWithoutArguments != null ? mDefaultWithoutArguments.mSubCommandName : null,
				mDefaultWithArguments != null ? mDefaultWithArguments.mSubCommandName : null,
				mSubCommands.keySet());
		moduleInformation.add(command);

		for (Map.Entry<String, SubCommand> entry : mSubCommands.entrySet()) {
			CommandHelp commandHelp = entry.getValue().getCommandHelp(context);
			moduleInformation.add(commandHelp);
		}
	}

	public static final synchronized <T extends SubCommand> void register(Class<T> subCommandClass,
			Set<SupraCommand> commands) {
		T subCommand;
		try {
			Constructor<T> constructor = subCommandClass.getConstructor();
			subCommand = constructor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		SupraCommand supraCommand = subCommand.mSupraCommand;
		commands.add(supraCommand);

		if (supraCommand.mSubCommands.containsKey(subCommand.mSubCommandName))
			throw new IllegalStateException("SubCommand name already registered.");
		if (subCommand.mIsDefaultWithArguments && supraCommand.mDefaultWithArguments != null)
			throw new IllegalStateException("SubCommand default with arguments already registered.");
		if (subCommand.mIsDefaultWithoutArguments && supraCommand.mDefaultWithoutArguments != null)
			throw new IllegalStateException(
					"SubCommand default without arguments already resitered.");

		supraCommand.mSubCommands.put(subCommand.mSubCommandName, subCommand);
		if (subCommand.mIsDefaultWithArguments) supraCommand.mDefaultWithArguments = subCommand;
		if (subCommand.mIsDefaultWithoutArguments)
			supraCommand.mDefaultWithoutArguments = subCommand;
	}
}
