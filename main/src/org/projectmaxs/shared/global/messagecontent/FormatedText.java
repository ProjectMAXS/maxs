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

import org.projectmaxs.shared.global.util.ParcelUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class FormatedText implements Parcelable {

	enum Font {
		// @formatter:off
		Standard,
		Monospace
		// @formatter:on
	}

	private final Font mFont;
	private final String mText;

	private boolean mIsBold = false;
	private boolean mIsItalic = false;

	public FormatedText(String text) {
		mText = text;
		mFont = Font.Standard;
	}

	private FormatedText(Parcel in) {
		mText = in.readString();
		mFont = Font.values()[in.readInt()];
		mIsBold = ParcelUtil.readBool(in);
		mIsItalic = ParcelUtil.readBool(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mText);
		dest.writeInt(mFont.ordinal());
		ParcelUtil.writeBool(dest, mIsBold);
		ParcelUtil.writeBool(dest, mIsItalic);
	}

	@Override
	public String toString() {
		return mText;
	}

	public static final Creator<FormatedText> CREATOR = new Creator<FormatedText>() {

		@Override
		public FormatedText createFromParcel(Parcel source) {
			return new FormatedText(source);
		}

		@Override
		public FormatedText[] newArray(int size) {
			return new FormatedText[size];
		}

	};
}
