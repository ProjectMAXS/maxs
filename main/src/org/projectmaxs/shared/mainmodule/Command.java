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

import org.projectmaxs.shared.global.Message;

import android.os.Parcel;
import android.os.Parcelable;

public class Command implements Parcelable {
	private final String mCommand;
	private final String mSubCommand;
	private final String mArgs;
	private final int mId;

	/**
	 * Dummy constructor
	 * 
	 * Only use if you know it's needed
	 */
	public Command() {
		mCommand = null;
		mSubCommand = null;
		mArgs = "";
		mId = Message.NO_ID;
	}

	public Command(String command, String subCommand, String args, int id) {
		this.mCommand = command;
		this.mSubCommand = subCommand;
		this.mArgs = args;
		this.mId = id;
	}

	public String getCommand() {
		return mCommand;
	}

	public String getSubCommand() {
		return mSubCommand;
	}

	/**
	 * Retrieve the arguments that the user send with the command. May be null
	 * 
	 * @return arguments given by user, may be null
	 */
	public String getArgs() {
		return mArgs;
	}

	public int getId() {
		return mId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mCommand);
		dest.writeString(mSubCommand);
		dest.writeString(mArgs);
		dest.writeInt(mId);
	}

	public static final Creator<Command> CREATOR = new Creator<Command>() {

		@Override
		public Command createFromParcel(Parcel source) {
			String command = source.readString();
			String subCommand = source.readString();
			String args = source.readString();
			int id = source.readInt();
			return new Command(command, subCommand, args, id);
		}

		@Override
		public Command[] newArray(int size) {
			return new Command[size];
		}

	};

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('\'').append(mCommand).append(' ').append(mSubCommand);
		if (mArgs != null) {
			sb.append(' ').append(mArgs);
		}
		sb.append(" (cmdId=").append(mId).append(")'");
		return sb.toString();
	}
}
