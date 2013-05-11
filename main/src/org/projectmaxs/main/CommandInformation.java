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

import org.projectmaxs.shared.ModuleInformation.Command;

public class CommandInformation {
	private String mCommand;
	private String mDefaultSubCommand;
	private String mDefaultSubCommandWithArgs;
	private final Map<String, String> mSubCommands = new HashMap<String, String>();

	public CommandInformation(String command) {
		this.mCommand = command;
	}

	public void addSubAndDefCommands(Command command, String modulePackage) throws CommandClashException {
		String defSubCmd = command.getDefaultSubCommand();
		if (mDefaultSubCommand != null && defSubCmd != null) {
			throw new DefaultCommandAlreadySet();
		}
		else if (defSubCmd != null) {
			mDefaultSubCommand = defSubCmd;
		}

		String defSubCmdArgs = command.getDefaultSubCommandWithArgs();
		if (mDefaultSubCommandWithArgs != null && defSubCmdArgs != null) {
			throw new DefaultCommandArgsAlreadySet();
		}
		else if (defSubCmdArgs != null) {
			mDefaultSubCommandWithArgs = defSubCmdArgs;
		}

		Set<String> subCmds = command.getSubCommands();
		for (String s : subCmds) {
			if (mSubCommands.containsKey(s)) throw new CommandAlreadyDefined();
			mSubCommands.put(s, modulePackage);
		}
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

	}

	@SuppressWarnings("serial")
	public static class DefaultCommandAlreadySet extends CommandClashException {

	}

	@SuppressWarnings("serial")
	public static class DefaultCommandArgsAlreadySet extends CommandClashException {

	}

	@SuppressWarnings("serial")
	public static class CommandAlreadyDefined extends CommandClashException {

	}
}
