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

import java.util.List;
import java.util.regex.Pattern;

import org.projectmaxs.shared.global.util.ParcelUtil;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

public class ContactNumber implements Parcelable {

	private static final Pattern numberPattern = Pattern.compile("\\+?\\d+");

	final String mNumber;
	final NumberType mNumberType;

	boolean mIsPrimary = false;
	String mLabel;

	public ContactNumber(String number) {
		this(NumberType.UNKOWN, number);
	}

	public ContactNumber(NumberType type, String number) {
		mNumberType = type;
		mNumber = cleanNumber(number);
	}

	public ContactNumber(String number, int type, String label) {
		mNumber = cleanNumber(number);
		mNumberType = fromInt(type);
		mLabel = label;
	}

	private ContactNumber(Parcel in) {
		mNumberType = in.readParcelable(NumberType.class.getClassLoader());
		mNumber = in.readString();
		mIsPrimary = ParcelUtil.readBool(in);
		mLabel = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(mNumberType, flags);
		dest.writeString(mNumber);
		ParcelUtil.writeBool(dest, mIsPrimary);
		dest.writeString(mLabel);
	}

	public static final Creator<ContactNumber> CREATOR = new Creator<ContactNumber>() {

		@Override
		public ContactNumber createFromParcel(Parcel source) {
			return new ContactNumber(source);
		}

		@Override
		public ContactNumber[] newArray(int size) {
			return new ContactNumber[size];
		}

	};

	static enum NumberType implements Parcelable {
		MOBILE, HOME, WORK, UNKOWN, OTHER;

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

	@Override
	public String toString() {
		return "ContactNumber number=" + mNumber + " type=" + mNumberType + " primary=" + mIsPrimary;
	}

	/**
	 * 
	 * @param numbers
	 * @return the best number, or null if none found
	 */
	public static ContactNumber getBest(List<ContactNumber> numbers) {
		if (numbers.isEmpty()) return null;
		for (ContactNumber number : numbers)
			if (number.mIsPrimary) return number;

		return numbers.get(0);
	}

	public static String cleanNumber(String number) {
		// @formatter:off
		return number
				.replace("(", "")
				.replace(")", "")
				.replace("-", "")
				.replace(".", "")
				.replace(" ", "");
		// @formatter:on
	}

	public static boolean isNumber(String s) {
		return numberPattern.matcher(cleanNumber(s)).matches();
	}

	public static NumberType fromInt(int i) {
		switch (i) {
		case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
			return NumberType.HOME;
		case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
			return NumberType.MOBILE;
		case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
			return NumberType.WORK;
		case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
			return NumberType.OTHER;
		default:
			return NumberType.UNKOWN;
		}
	}
}
