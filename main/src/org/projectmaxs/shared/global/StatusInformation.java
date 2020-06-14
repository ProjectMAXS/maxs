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

package org.projectmaxs.shared.global;

import android.os.Parcel;
import android.os.Parcelable;

public class StatusInformation implements Parcelable {

	private final String mStatusKey;
	private final String mHumanValue;
	private final String mMachineValue;

	public String getKey() {
		return mStatusKey;
	}

	public String getHumanValue() {
		return mHumanValue;
	}

	public String getMachineValue() {
		return mMachineValue;
	}

	public StatusInformation(String statusKey, String statusValue) {
		this(statusKey, statusValue, statusValue);
	}

	public StatusInformation(String statusKey, String humanValue, String machineValue) {
		if (statusKey.contains(" "))
			throw new IllegalStateException("StatusInformation key='" + statusKey
					+ "' must not contain whitespace");
		if (statusKey.isEmpty()) {
			throw new IllegalStateException("Status key must not be empty");
		}
		if (machineValue.isEmpty()) {
			throw new IllegalStateException("Status machine value must not be empty");
		}
		this.mStatusKey = statusKey;
		if (humanValue != null && humanValue.isEmpty()) {
			throw new IllegalStateException("If a human value is given, it must not be empty");
		}
		this.mHumanValue = humanValue;
		this.mMachineValue = machineValue;
	}

	private StatusInformation(Parcel in) {
		mStatusKey = in.readString();
		mHumanValue = in.readString();
		mMachineValue = in.readString();
	}

	@Override
	public String toString() {
		return "StatusInformation(key: " + mStatusKey + ", human: " + mHumanValue + ", machine: " + mMachineValue + ")";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mStatusKey);
		dest.writeString(mHumanValue);
		dest.writeString(mMachineValue);
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
