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

	public static final FormatedText SINGLE_SPACE = new FormatedText(" ");

	private final String mText;

	/**
	 * The used font-family
	 */
	private String mFont;
	private boolean mIsBold = false;
	private boolean mIsItalic = false;

	public FormatedText(String text) {
		mText = text;
	}

	public FormatedText(CharSequence charSequence) {
		this(charSequence.toString());
	}

	private FormatedText(Parcel in) {
		mText = in.readString();
		mFont = in.readString();
		mIsBold = ParcelUtil.readBool(in);
		mIsItalic = ParcelUtil.readBool(in);
	}

	public FormatedText makeBold() {
		mIsBold = true;
		return this;
	}

	public boolean isBold() {
		return mIsBold;
	}

	public FormatedText makeItalic() {
		mIsItalic = true;
		return this;
	}

	public boolean isItalic() {
		return mIsItalic;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mText);
		dest.writeString(mFont);
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

	public static FormatedText from(CharSequence cs) {
		return new FormatedText(cs);
	}

	public static FormatedText bold(CharSequence cs) {
		return FormatedText.from(cs).makeBold();
	}

	public static FormatedText italic(CharSequence cs) {
		return FormatedText.from(cs).makeItalic();
	}

	public static boolean isNewLine(FormatedText formatedText) {
		return formatedText == NewLine.getInstance();
	}
}
