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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
	private final String mName;
	private final List<ContactNumber> mNumbers;

	public Contact(String contactNumber) {
		mName = "";
		List<ContactNumber> numbers = new ArrayList<ContactNumber>(1);
		numbers.add(new ContactNumber(contactNumber));
		mNumbers = numbers;
	}

	private Contact(String name, List<ContactNumber> numbers) {
		this.mName = name;
		this.mNumbers = numbers;
	}

	private Contact(Parcel in) {
		String name = in.readString();
		ContactNumber[] numbers = (ContactNumber[]) in.readParcelableArray(ContactNumber.class.getClassLoader());
		List<ContactNumber> numbersList = Arrays.asList(numbers);
		this.mName = name;
		this.mNumbers = numbersList;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mName);
		ContactNumber[] numbers = new ContactNumber[mNumbers.size()];
		mNumbers.toArray(numbers);
		dest.writeParcelableArray(numbers, flags);
	}

	public static final Creator<Contact> CREATOR = new Creator<Contact>() {

		@Override
		public Contact createFromParcel(Parcel source) {
			return new Contact(source);
		}

		@Override
		public Contact[] newArray(int size) {
			return new Contact[size];
		}

	};

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Contact ");
		sb.append("name=" + (mName.equals("") ? "noName" : mName));

		ContactNumber number = ContactNumber.getBest(mNumbers);
		if (number != null) sb.append(" bestNumber='" + number.toString() + "'");

		return sb.toString();
	}
}
