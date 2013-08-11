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

import android.content.ComponentName;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class TransportOrigin implements Parcelable {

	private final String mPackage;
	private final String mIntentAction;

	public TransportOrigin(String pkg, String intentAction) {
		mPackage = pkg;
		mIntentAction = intentAction;
	}

	private TransportOrigin(Parcel in) {
		mPackage = in.readString();
		mIntentAction = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mPackage);
		dest.writeString(mIntentAction);
	}

	public static final Creator<TransportOrigin> CREATOR = new Creator<TransportOrigin>() {

		@Override
		public TransportOrigin createFromParcel(Parcel source) {
			return new TransportOrigin(source);
		}

		@Override
		public TransportOrigin[] newArray(int size) {
			return new TransportOrigin[size];
		}

	};

	public String getPackage() {
		return mPackage;
	}

	public String getIntentAction() {
		return mIntentAction;
	}

	public String getServiceClass() {
		return mPackage + TransportConstants.TRANSPORT_SERVICE;
	}

	public Intent getIntentFor() {
		Intent intent = new Intent(mIntentAction);
		intent.setComponent(new ComponentName(mPackage, getServiceClass()));
		return intent;
	}
}
