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

package org.projectmaxs.shared.maintransport;

import java.util.Collections;
import java.util.List;

import org.projectmaxs.shared.global.StatusInformation;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The current status reported by MAXS. Consisting of a human readable status string and
 * (optionally) a list of {@link StatusInformation} with the detailed single status informatino.
 * 
 */
public class CurrentStatus implements Parcelable {
	private final String statusString;
	private final List<StatusInformation> statusInformationList;

	public CurrentStatus(String statusString, List<StatusInformation> statusInformationList) {
		if (statusString.isEmpty()) {
			throw new IllegalArgumentException();
		}
		if (statusInformationList.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.statusString = statusString;
		this.statusInformationList = Collections.unmodifiableList(statusInformationList);
	}

	public String getStatusString() {
		return statusString;
	}

	public List<StatusInformation> getStatusInformationList() {
		return statusInformationList;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(statusString);
		dest.writeList(statusInformationList);
	}

	public static final Creator<CurrentStatus> CREATOR = new Creator<CurrentStatus>() {

		@Override
		public CurrentStatus createFromParcel(Parcel source) {
			String statusString = source.readString();
			@SuppressWarnings("unchecked")
			List<StatusInformation> statusInformationList = source
					.readArrayList(getClass().getClassLoader());
			return new CurrentStatus(statusString, statusInformationList);
		}

		@Override
		public CurrentStatus[] newArray(int size) {
			return new CurrentStatus[size];
		}

	};
}
