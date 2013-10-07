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

package org.projectmaxs.shared.global.messagecontent;

import android.os.Parcel;

public class CommandHelp extends AbstractElement {

	public final String mCommand;
	public final String mSubCommand;
	public final ArgType mArgType;
	public final String mArgString;
	public final String mHelp;

	public CommandHelp(String command, String subCommand, ArgType argType, String help) {
		mCommand = command;
		mSubCommand = subCommand;
		mArgType = argType;
		mArgString = null;
		mHelp = help;
	}

	public CommandHelp(String command, String subCommand, String argString, String help) {
		mCommand = command;
		mSubCommand = subCommand;
		mArgType = ArgType.OTHER_STRING;
		mArgString = argString;
		mHelp = help;
	}

	private CommandHelp(Parcel in) {
		mCommand = in.readString();
		mSubCommand = in.readString();
		mArgType = ArgType.values()[in.readInt()];
		if (mArgType == ArgType.OTHER_STRING) {
			mArgString = in.readString();
		} else {
			mArgString = null;
		}
		mHelp = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mCommand);
		dest.writeString(mSubCommand);
		dest.writeInt(mArgType.ordinal());
		if (mArgType == ArgType.OTHER_STRING) dest.writeString(mArgString);
		dest.writeString(mHelp);
	}

	public static final Creator<CommandHelp> CREATOR = new Creator<CommandHelp>() {

		@Override
		public CommandHelp createFromParcel(Parcel source) {
			return new CommandHelp(source);
		}

		@Override
		public CommandHelp[] newArray(int size) {
			return new CommandHelp[size];
		}

	};

	public static enum ArgType {
		FILE, PATH, NUMBER, CONTACT_INFO, CONTACT_NICKNAME, CONTACT_NAME, OTHER_STRING, NONE,
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		if (this == o) return true;

		CommandHelp other = (CommandHelp) o;
		if (other.hashCode() == hashCode()) return true;
		return false;
	}

	@Override
	public int hashCode() {
		return mCommand.hashCode() + 31 * mSubCommand.hashCode();
	}

}
