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

public class Sms extends AbstractElement {

	private final String mContactString;
	private final String mBody;
	private final Direction mDirection;

	public Sms(String contact, String body, Direction direction) {
		mContactString = contact;
		mBody = body;
		mDirection = direction;
	}

	public Sms(Parcel in) {
		int directionInt = in.readInt();
		mDirection = Direction.values()[directionInt];
		mContactString = in.readString();
		mBody = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mDirection.ordinal());
		dest.writeString(mContactString);
		dest.writeString(mBody);
	}

	public static final Creator<Sms> CREATOR = new Creator<Sms>() {

		@Override
		public Sms createFromParcel(Parcel source) {
			return new Sms(source);
		}

		@Override
		public Sms[] newArray(int size) {
			return new Sms[size];
		}
	};

	public Direction getDirection() {
		return mDirection;
	}

	public String getContact() {
		return mContactString;
	}

	public String getBody() {
		return mBody;
	}

	public static enum Direction {
		INCOMING, OUTGOING,
	}

}
