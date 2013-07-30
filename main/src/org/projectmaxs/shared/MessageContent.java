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

public class MessageContent implements Parcelable {

	StringBuilder mS;

	public MessageContent() {
		this(256);
	}

	public MessageContent(int stringBuilderSize) {
		mS = new StringBuilder(stringBuilderSize);
	}

	public MessageContent(String string) {
		this(string.length());
		mS.append(string);
	}

	public MessageContent add(String string) {
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

	public static final Creator<MessageContent> CREATOR = new Creator<MessageContent>() {

		@Override
		public MessageContent createFromParcel(Parcel source) {
			String s = source.readString();
			return new MessageContent(s);
		}

		@Override
		public MessageContent[] newArray(int size) {
			return new MessageContent[size];
		}

	};

}
