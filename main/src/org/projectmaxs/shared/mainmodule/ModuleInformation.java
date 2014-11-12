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

package org.projectmaxs.shared.mainmodule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.projectmaxs.shared.global.messagecontent.CommandHelp;
import org.projectmaxs.shared.global.util.SharedStringUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class ModuleInformation implements Parcelable, Comparable<ModuleInformation> {
	private final String mModulePackage;
	private final String mModuleName;
	private final Set<Command> mCommands;
	private final Set<CommandHelp> mHelp = new HashSet<CommandHelp>();

	public ModuleInformation(String modulePackage, String moduleName) {
		mModulePackage = modulePackage;
		mModuleName = moduleName;
		mCommands = new HashSet<Command>();
	}

	public ModuleInformation(String modulePackage) {
		this(modulePackage, SharedStringUtil.getSubstringAfter(modulePackage, '.'));
	}

	public ModuleInformation(Parcel in) {
		mModulePackage = in.readString();
		mModuleName = in.readString();
		@SuppressWarnings("unchecked")
		List<Command> cmds = in.readArrayList(getClass().getClassLoader());
		mCommands = new HashSet<Command>(cmds);
		@SuppressWarnings("unchecked")
		List<CommandHelp> help = in.readArrayList(getClass().getClassLoader());
		mHelp.addAll(help);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mModulePackage);
		dest.writeString(mModuleName);
		dest.writeList(new ArrayList<Command>(mCommands));
		dest.writeList(new ArrayList<CommandHelp>(mHelp));
	}

	public String getModulePackage() {
		return mModulePackage;
	}

	public String getModuleName() {
		return mModuleName;
	}

	public void add(Command command) {
		if (command == null) return;
		mCommands.add(command);
	}

	public void add(CommandHelp commandHelp) {
		if (commandHelp == null) return;
		mHelp.add(commandHelp);
	}

	public Set<Command> getCommands() {
		return mCommands;
	}

	public Set<CommandHelp> getHelp() {
		return mHelp;
	}

	public String toString() {
		return "Package: " + mModulePackage;
	}

	public boolean provides(String command, String subCommand) {
		for (Command cmd : mCommands)
			if (cmd.getCommand().equals(command) && cmd.mSubCommands.contains(subCommand))
				return true;

		return false;
	}

	public static final Creator<ModuleInformation> CREATOR = new Creator<ModuleInformation>() {

		@Override
		public ModuleInformation createFromParcel(Parcel source) {
			return new ModuleInformation(source);
		}

		@Override
		public ModuleInformation[] newArray(int size) {
			return new ModuleInformation[size];
		}

	};

	@Override
	public int compareTo(ModuleInformation another) {
		final int nameCompare = this.mModuleName.compareTo(another.mModuleName);
		if (nameCompare != 0) return nameCompare;

		return this.mModulePackage.compareTo(another.mModulePackage);
	}

	public static class Command implements Parcelable {

		private final String mCommand;
		private final String mShortCommand;
		private final String mDefaultSubCommand;
		private final String mDefaultSubCommandWithArgs;
		private Set<String> mSubCommands;

		private Command(String command, String shortCommand, String defaultSubCommand,
				String defaultSubCommandWithArgs) {
			mCommand = command;
			mShortCommand = shortCommand;
			mDefaultSubCommand = defaultSubCommand;
			mDefaultSubCommandWithArgs = defaultSubCommandWithArgs;
		}

		public Command(String command, String shortCommand, String defaultSubCommand,
				String defaultSubcommandWithArgs, Set<String> subCommands) {
			this(command, shortCommand, defaultSubCommand, defaultSubcommandWithArgs);
			mSubCommands = subCommands;
		}

		public Command(String command, String shortCommand, String defaultSubCommand,
				String defaultSubcommandWithArgs, String... subCommands) {
			this(command, shortCommand, defaultSubCommand, defaultSubcommandWithArgs);
			Set<String> subCmdSet = new HashSet<String>();
			for (String s : subCommands)
				subCmdSet.add(s);
			mSubCommands = subCmdSet;
		}

		public Command(Parcel in) {
			mCommand = in.readString();
			mShortCommand = in.readString();
			mDefaultSubCommand = in.readString();
			mDefaultSubCommandWithArgs = in.readString();
			String[] subCmdsArray = in.createStringArray();
			mSubCommands = new HashSet<String>(Arrays.asList(subCmdsArray));
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(mCommand);
			dest.writeString(mShortCommand);
			dest.writeString(mDefaultSubCommand);
			dest.writeString(mDefaultSubCommandWithArgs);
			String[] subCommands = mSubCommands.toArray(new String[mSubCommands.size()]);
			dest.writeStringArray(subCommands);
		}

		public String getCommand() {
			return mCommand;
		}

		public String getShortCommand() {
			return mShortCommand;
		}

		public String getDefaultSubCommand() {
			return mDefaultSubCommand;
		}

		public String getDefaultSubCommandWithArgs() {
			return mDefaultSubCommandWithArgs;
		}

		public Set<String> getSubCommands() {
			return mSubCommands;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		public static final Creator<Command> CREATOR = new Creator<Command>() {

			@Override
			public Command createFromParcel(Parcel source) {
				return new Command(source);
			}

			@Override
			public Command[] newArray(int size) {
				return new Command[size];
			}

		};

		public int hashCode() {
			return mCommand.hashCode();
		}

		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) return false;
			if (this == o) return true;

			Command other = (Command) o;
			if (other.hashCode() == hashCode()) return true;
			return false;
		}

	}

}
