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

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class CommandOrigin implements Parcelable {

	private final String mPackage;
	private final String mIntentAction;
	private final String mOriginIssuerInfo;
	private final String mOriginId;

	public CommandOrigin(String pkg, String intentAction, String originIssuerInfo, String originId) {
		mPackage = pkg;
		mIntentAction = intentAction;
		mOriginIssuerInfo = originIssuerInfo;
		mOriginId = originId;
	}

	private CommandOrigin(Parcel in) {
		mPackage = in.readString();
		mIntentAction = in.readString();
		mOriginIssuerInfo = in.readString();
		mOriginId = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mPackage);
		dest.writeString(mIntentAction);
		dest.writeString(mOriginIssuerInfo);
		dest.writeString(mOriginId);
	}

	public static final Creator<CommandOrigin> CREATOR = new Creator<CommandOrigin>() {

		@Override
		public CommandOrigin createFromParcel(Parcel source) {
			return new CommandOrigin(source);
		}

		@Override
		public CommandOrigin[] newArray(int size) {
			return new CommandOrigin[size];
		}

	};

	public String getPackage() {
		return mPackage;
	}

	public String getIntentAction() {
		return mIntentAction;
	}

	public String getOriginIssuerInfo() {
		return mOriginIssuerInfo;
	}

	public String getOriginId() {
		return mOriginId;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CommandOrigin package=" + mPackage);
		sb.append(" act=" + mIntentAction);
		sb.append(" issuerInfo=" + mOriginIssuerInfo);
		sb.append(" id=" + mOriginId);
		return sb.toString();
	}

	public String getServiceClass() {
		return mPackage + TransportConstants.TRANSPORT_SERVICE;
	}

	public Intent getIntentFor() {
		Intent intent = new Intent(mIntentAction);
		intent.setClassName(mPackage, getServiceClass());
		intent.putExtra(TransportConstants.EXTRA_COMMAND_ORIGIN, this);
		return intent;
	}
}
