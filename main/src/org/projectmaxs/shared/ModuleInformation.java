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

package org.projectmaxs.shared;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.os.Parcel;
import android.os.Parcelable;

public class ModuleInformation implements Parcelable {
	String applicationPackage;
	Set<Command> mCommands;

	public ModuleInformation(String appPackage, Set<Command> commands) {
		this.applicationPackage = appPackage;
		this.mCommands = commands;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(applicationPackage);

		Command[] cmds = new Command[mCommands.size()];
		mCommands.toArray(cmds);
		dest.writeParcelableArray(cmds, flags);
	}

	public static final Creator<ModuleInformation> CREATOR = new Creator<ModuleInformation>() {

		@Override
		public ModuleInformation createFromParcel(Parcel source) {
			String appPackage = source.readString();
			Command[] cmds = (Command[]) source.readParcelableArray(Command.class.getClassLoader());
			Set<Command> cmdSet = new HashSet<Command>(Arrays.asList(cmds));
			return new ModuleInformation(appPackage, cmdSet);
		}

		@Override
		public ModuleInformation[] newArray(int size) {
			return new ModuleInformation[size];
		}

	};

	static class Command implements Parcelable {

		String mCommand;
		String mDefaultSubCommand;
		String mDefaultSubCommandWithArgs;
		Set<String> mSubCommands;

		public Command(String command, String defaultSubCommand, String defaultSubcommandWithArgs,
				Set<String> subCommands) {
			this.mCommand = command;
			this.mDefaultSubCommand = defaultSubCommand;
			this.mDefaultSubCommandWithArgs = defaultSubcommandWithArgs;
			this.mSubCommands = subCommands;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(mCommand);
			dest.writeString(mDefaultSubCommand);
			dest.writeString(mDefaultSubCommandWithArgs);
			String[] subCmds = mSubCommands.toArray(new String[mSubCommands.size()]);
			// TODO describe better what is going on
			// Bad Bad Android API, we have to encode the length 2 times.
			// It's actually also encoded in the Array, readStringArray() method
			// bails out if the given array is to small
			dest.writeInt(subCmds.length);
			dest.writeStringArray(subCmds);
		}

		public static final Creator<Command> CREATOR = new Creator<Command>() {

			@Override
			public Command createFromParcel(Parcel source) {
				String cmd = source.readString();
				String defaultSubCommand = source.readString();
				String defaultSubCommandWithArgs = source.readString();
				int size = source.readInt();
				String[] subCmdsArray = new String[size];
				source.readStringArray(subCmdsArray);
				Set<String> subCmds = new HashSet<String>(Arrays.asList(subCmdsArray));
				return new Command(cmd, defaultSubCommand, defaultSubCommandWithArgs, subCmds);
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
