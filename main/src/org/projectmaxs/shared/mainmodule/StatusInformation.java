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

import android.os.Parcel;
import android.os.Parcelable;

public class StatusInformation implements Parcelable {

	private final String mStatusKey;
	private final String mStatusValue;

	public String getKey() {
		return mStatusKey;
	}

	public String getValue() {
		return mStatusValue;
	}

	public StatusInformation(String statusKey, String statusValue) {
		if (statusKey.contains(" "))
			throw new IllegalStateException("StatusInformation key='" + statusKey
					+ "' must not contain whitespace");
		this.mStatusKey = statusKey;
		this.mStatusValue = statusValue;
	}

	private StatusInformation(Parcel in) {
		mStatusKey = in.readString();
		mStatusValue = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mStatusKey);
		dest.writeString(mStatusValue);
	}

	public static final Creator<StatusInformation> CREATOR = new Creator<StatusInformation>() {

		@Override
		public StatusInformation createFromParcel(Parcel source) {
			return new StatusInformation(source);
		}

		@Override
		public StatusInformation[] newArray(int size) {
			return new StatusInformation[size];
		}

	};
}
