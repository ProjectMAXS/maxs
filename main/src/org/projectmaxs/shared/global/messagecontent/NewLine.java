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
import android.os.Parcelable;

/**
 * 
 * Useful to sign that a </br> is needed in XHTML-IM
 * 
 */
public class NewLine extends FormatedText implements Parcelable {

	static final NewLine sInstance = new NewLine();

	public static NewLine getInstance() {
		return sInstance;
	}

	private NewLine() {
		super("\n");
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {}

	public static final Creator<NewLine> CREATOR = new Creator<NewLine>() {

		@Override
		public NewLine createFromParcel(Parcel source) {
			return getInstance();
		}

		@Override
		public NewLine[] newArray(int size) {
			return new NewLine[size];
		}

	};
}
