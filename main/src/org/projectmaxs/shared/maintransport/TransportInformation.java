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

import java.util.ArrayList;
import java.util.List;

import org.projectmaxs.shared.global.util.ParcelUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class TransportInformation implements Parcelable, Comparable<TransportInformation> {
	private final String mTransportPackage;
	private final String mTransportName;
	private final List<TransportComponent> mComponents;

	public TransportInformation(String transportPackage, String transportName) {
		mTransportPackage = transportPackage;
		mTransportName = transportName;
		mComponents = new ArrayList<TransportComponent>(2);
	}

	@SuppressWarnings("unchecked")
	public TransportInformation(Parcel in) {
		mTransportPackage = in.readString();
		mTransportName = in.readString();
		mComponents = in.readArrayList(getClass().getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mTransportPackage);
		dest.writeString(mTransportName);
		dest.writeList(mComponents);
	}

	public String getTransportPackage() {
		return mTransportPackage;
	}

	public String getTransportName() {
		return mTransportName;
	}

	public String toString() {
		return "Package: " + mTransportPackage;
	}

	public static final Creator<TransportInformation> CREATOR = new Creator<TransportInformation>() {

		@Override
		public TransportInformation createFromParcel(Parcel source) {
			return new TransportInformation(source);
		}

		@Override
		public TransportInformation[] newArray(int size) {
			return new TransportInformation[size];
		}

	};

	@Override
	public int compareTo(TransportInformation another) {
		final int nameCompare = this.mTransportName.compareTo(another.mTransportName);
		if (nameCompare != 0) return nameCompare;

		return this.mTransportPackage.compareTo(another.mTransportPackage);
	}

	static class TransportComponent implements Parcelable {
		private final String mName;
		private final String mClass;
		private final boolean mFeatureBroadcast;
		private final boolean mFeatureStatus;

		public TransportComponent(String name, String cls, boolean broadcast, boolean status) {
			mName = name;
			mClass = cls;
			mFeatureBroadcast = broadcast;
			mFeatureStatus = status;
		}

		private TransportComponent(Parcel in) {
			mName = in.readString();
			mClass = in.readString();
			mFeatureBroadcast = ParcelUtil.readBool(in);
			mFeatureStatus = ParcelUtil.readBool(in);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(mName);
			dest.writeString(mClass);
			ParcelUtil.writeBool(dest, mFeatureBroadcast);
			ParcelUtil.writeBool(dest, mFeatureStatus);
		}

		public static final Creator<TransportComponent> CREATOR = new Creator<TransportComponent>() {

			@Override
			public TransportComponent createFromParcel(Parcel source) {
				return new TransportComponent(source);
			}

			@Override
			public TransportComponent[] newArray(int size) {
				return new TransportComponent[size];
			}

		};

		public String getName() {
			return mName;
		}

		public String getClassName() {
			return mClass;
		}

		public boolean isBroadcastSupported() {
			return mFeatureBroadcast;
		}

		public boolean isStatusSupported() {
			return mFeatureStatus;
		}
	}

}
