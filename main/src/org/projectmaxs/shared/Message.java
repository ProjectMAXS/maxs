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

public class Message implements Parcelable {

	StringBuilder mS;

	public Message() {
		this(256);
	}

	public Message(int stringBuilderSize) {
		mS = new StringBuilder(stringBuilderSize);
	}

	public Message(String string) {
		this(string.length());
		mS.append(string);
	}

	public Message add(String string) {
		mS.append(string);
		return this;
	}

	public String getRawContent() {
		return mS.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeString(mS.toString());
	}

	public static final Creator<Message> CREATOR = new Creator<Message>() {

		@Override
		public Message createFromParcel(Parcel source) {
			String s = source.readString();
			return new Message(s);
		}

		@Override
		public Message[] newArray(int size) {
			return new Message[size];
		}

	};

}
