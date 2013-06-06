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

import android.os.Parcel;
import android.os.Parcelable;

public class UserMessage implements Parcelable {
	public static final int NO_ID = -1;

	private final int mId;
	private final Message message;

	public UserMessage(Message msg) {
		this(msg, NO_ID);
	}

	public UserMessage(Message msg, int id) {
		this.mId = id;
		this.message = msg;
	}

	public UserMessage(String string) {
		this(string, NO_ID);
	}

	public UserMessage(String string, int id) {
		this.mId = id;
		this.message = new Message(string);
	}

	public int getId() {
		return mId;
	}

	public Message geMessage() {
		return message;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeParcelable(message, flags);
	}

	public static final Creator<UserMessage> CREATOR = new Creator<UserMessage>() {

		@Override
		public UserMessage createFromParcel(Parcel source) {
			int id = source.readInt();
			Message msg = source.readParcelable(Message.class.getClassLoader());
			return new UserMessage(msg, id);
		}

		@Override
		public UserMessage[] newArray(int size) {
			return new UserMessage[size];
		}

	};

}
