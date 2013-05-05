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

import java.util.Arrays;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
	private String mName;
	private List<Number> mNumbers;

	private Contact(String name, List<Number> numbers) {
		this.mName = name;
		this.mNumbers = numbers;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mName);
		Number[] numbers = new Number[mNumbers.size()];
		mNumbers.toArray(numbers);
		dest.writeParcelableArray(numbers, flags);
	}

	public static final Creator<Contact> CREATOR = new Creator<Contact>() {

		@Override
		public Contact createFromParcel(Parcel source) {
			String name = source.readString();
			Number[] numbers = (Contact.Number[]) source.readParcelableArray(Number.class.getClassLoader());
			List<Number> numbersList = Arrays.asList(numbers);
			return new Contact(name, numbersList);
		}

		@Override
		public Contact[] newArray(int size) {
			return new Contact[size];
		}

	};

	static class Number implements Parcelable {

		NumberType mNumberType;
		String mNumber;

		public Number(NumberType type, String number) {
			this.mNumberType = type;
			this.mNumber = number;
		}

		private Number(Parcel in) {
			mNumberType = in.readParcelable(NumberType.class.getClassLoader());
			mNumber = in.readString();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeParcelable(mNumberType, flags);
			dest.writeString(mNumber);
		}

		public static final Creator<Number> CREATOR = new Creator<Number>() {

			@Override
			public Number createFromParcel(Parcel source) {
				return new Number(source);
			}

			@Override
			public Number[] newArray(int size) {
				return new Number[size];
			}

		};

		enum NumberType implements Parcelable {
			MOBILE, HOME, WORK;

			@Override
			public int describeContents() {
				return 0;
			}

			@Override
			public void writeToParcel(Parcel dest, int flags) {
				dest.writeInt(ordinal());
			}

			public static final Creator<NumberType> CREATOR = new Creator<NumberType>() {

				@Override
				public NumberType createFromParcel(Parcel source) {
					return NumberType.values()[source.readInt()];
				}

				@Override
				public NumberType[] newArray(int size) {
					return new NumberType[size];
				}

			};
		}

	}

}
