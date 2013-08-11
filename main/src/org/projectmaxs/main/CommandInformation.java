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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.projectmaxs.shared.mainmodule.ModuleInformation.Command;

public class CommandInformation {
	private final String mCommand;
	private String mDefaultSubCommand;
	private String mDefaultSubCommandWithArgs;

	/**
	 * Map of SubCommands to packages that are responsible for handling them
	 */
	private final Map<String, String> mSubCommands = new HashMap<String, String>();

	public CommandInformation(String command) {
		this.mCommand = command;
	}

	public String getDefaultSubCommand() {
		return mDefaultSubCommand;
	}

	public String getDefaultSubcommandWithArgs() {
		return mDefaultSubCommandWithArgs;
	}

	public String getPackageForSubCommand(String subCommand) {
		return mSubCommands.get(subCommand);
	}

	public boolean isKnownSubCommand(String subCommand) {
		return mSubCommands.containsKey(subCommand);
	}

	public void addSubAndDefCommands(Command command, String modulePackage) throws CommandClashException {
		String defSubCmd = command.getDefaultSubCommand();
		if (mDefaultSubCommand != null && defSubCmd != null
				&& !modulePackage.equals(getPackageForSubCommand(mDefaultSubCommand))) {
			throw new DefaultCommandAlreadySet("Package " + modulePackage
					+ " is trying to override default sub command " + mDefaultSubCommand + " with " + defSubCmd
					+ ". Previous def sub command was set by " + getPackageForSubCommand(mDefaultSubCommand));
		}
		else if (defSubCmd != null) {
			mDefaultSubCommand = defSubCmd;
		}

		String defSubCmdArgs = command.getDefaultSubCommandWithArgs();
		if (mDefaultSubCommandWithArgs != null && defSubCmdArgs != null
				&& !modulePackage.equals(getPackageForSubCommand(mDefaultSubCommandWithArgs))) {
			throw new DefaultCommandArgsAlreadySet("Package " + modulePackage
					+ " is trying to override default sub command with args " + mDefaultSubCommandWithArgs + " with "
					+ defSubCmdArgs + ". Previous def sub command with args was set by "
					+ getPackageForSubCommand(mDefaultSubCommandWithArgs));
		}
		else if (defSubCmdArgs != null) {
			mDefaultSubCommandWithArgs = defSubCmdArgs;
		}

		Set<String> subCmds = command.getSubCommands();
		for (String s : subCmds) {
			if (mSubCommands.containsKey(s) && !modulePackage.equals(getPackageForSubCommand(s)))
				throw new CommandAlreadyDefined("Package " + modulePackage + " is tyring to override command " + s
						+ " which was previously defined by " + getPackageForSubCommand(s));
			mSubCommands.put(s, modulePackage);
		}
	}

	/**
	 * Removes all SubCommands for a given package. Also resets the default
	 * SubCommand and the default SubCommand with arguments if the are part of
	 * the removed commands. Returns true if the command is no orphan, that is
	 * when the command has no SubCommands any more
	 * 
	 * @param packageName
	 * @return true if the command is no orphan
	 */
	public boolean removeAllSubCommandsForPackage(String packageName) {
		boolean commandIsOrphan = false;

		Iterator<Entry<String, String>> it = mSubCommands.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			String entryPackageName = entry.getValue();
			if (packageName.equals(entryPackageName)) {
				String subCommand = entry.getKey();
				if (subCommand.equals(mDefaultSubCommand)) mDefaultSubCommand = null;
				if (subCommand.equals(mDefaultSubCommandWithArgs)) mDefaultSubCommandWithArgs = null;
				it.remove();
			}
		}

		if (mSubCommands.isEmpty()) commandIsOrphan = true;
		return commandIsOrphan;
	}

	public int hashCode() {
		return mCommand.hashCode();
	}

	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		if (this == o) return true;

		CommandInformation other = (CommandInformation) o;
		if (other.hashCode() == hashCode()) return true;
		return false;
	}

	@SuppressWarnings("serial")
	public static abstract class CommandClashException extends Exception {
		CommandClashException(String reason) {
			super(reason);
		}
	}

	@SuppressWarnings("serial")
	public static class DefaultCommandAlreadySet extends CommandClashException {
		DefaultCommandAlreadySet(String reason) {
			super(reason);
		}
	}

	@SuppressWarnings("serial")
	public static class DefaultCommandArgsAlreadySet extends CommandClashException {
		DefaultCommandArgsAlreadySet(String reason) {
			super(reason);
		}
	}

	@SuppressWarnings("serial")
	public static class CommandAlreadyDefined extends CommandClashException {
		CommandAlreadyDefined(String reason) {
			super(reason);
		}
	}
}
