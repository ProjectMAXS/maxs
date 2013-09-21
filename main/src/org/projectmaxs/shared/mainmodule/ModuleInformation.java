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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.projectmaxs.shared.global.util.SharedStringUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class ModuleInformation implements Parcelable, Comparable<ModuleInformation> {
	private final String mModulePackage;
	private final String mModuleName;
	private final Set<Command> mCommands;

	public ModuleInformation(String modulePackage, String moduleName) {
		this.mModulePackage = modulePackage;
		this.mModuleName = moduleName;
		this.mCommands = new HashSet<Command>();
	}

	public ModuleInformation(String modulePackage, Set<Command> commands) {
		this.mModulePackage = modulePackage;
		this.mModuleName = SharedStringUtil.getSubstringAfter(modulePackage, '.');
		this.mCommands = commands;
	}

	public ModuleInformation(String modulePackage, Command... commands) {
		this.mModulePackage = modulePackage;
		this.mModuleName = SharedStringUtil.getSubstringAfter(modulePackage, '.');
		Set<Command> cmds = new HashSet<Command>();
		for (Command c : commands)
			cmds.add(c);
		this.mCommands = cmds;
	}

	public ModuleInformation(String modulePackage, String moduleName, Set<Command> commands) {
		this.mModulePackage = modulePackage;
		this.mModuleName = moduleName;
		this.mCommands = commands;
	}

	public ModuleInformation(String modulePackage, String moduleName, Command... commands) {
		this.mModulePackage = modulePackage;
		this.mModuleName = moduleName;
		Set<Command> cmds = new HashSet<Command>();
		for (Command c : commands)
			cmds.add(c);
		this.mCommands = cmds;
	}

	public ModuleInformation(Parcel in) {
		mModulePackage = in.readString();
		mModuleName = in.readString();

		int cmdCount = in.readInt();
		Command[] cmds = new Command[cmdCount];
		in.readTypedArray(cmds, Command.CREATOR);
		mCommands = new HashSet<Command>(Arrays.asList(cmds));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mModulePackage);
		dest.writeString(mModuleName);
		int cmdCount = mCommands.size();
		Command[] cmds = new Command[cmdCount];
		cmds = mCommands.toArray(cmds);
		dest.writeInt(cmdCount);
		dest.writeTypedArray(cmds, flags);
	}

	public String getModulePackage() {
		return mModulePackage;
	}

	public String getModuleName() {
		return mModuleName;
	}

	public Set<Command> getCommands() {
		return mCommands;
	}

	public String toString() {
		return "Package: " + mModulePackage;
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
				String defaultSubcommandWithArgs) {
			this.mCommand = command;
			this.mShortCommand = shortCommand;
			this.mDefaultSubCommand = defaultSubCommand;
			this.mDefaultSubCommandWithArgs = defaultSubcommandWithArgs;
		}

		public Command(String command, String shortCommand, String defaultSubCommand,
				String defaultSubcommandWithArgs, Set<String> subCommands) {
			this(command, shortCommand, defaultSubCommand, defaultSubcommandWithArgs);
			this.mSubCommands = subCommands;
		}

		public Command(String command, String shortCommand, String defaultSubCommand,
				String defaultSubcommandWithArgs, String... subCommands) {
			this(command, shortCommand, defaultSubCommand, defaultSubcommandWithArgs);
			Set<String> subCmdSet = new HashSet<String>();
			for (String s : subCommands)
				subCmdSet.add(s);
			this.mSubCommands = subCmdSet;
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
