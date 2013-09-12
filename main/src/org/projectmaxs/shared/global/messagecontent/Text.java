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

import java.util.LinkedList;
import java.util.List;

import android.os.Parcel;

public class Text extends AbstractElement {

	private final List<FormatedText> mTexts = new LinkedList<FormatedText>();

	public Text(String text, boolean newLine) {
		mTexts.add(new FormatedText(text));
		if (newLine) mTexts.add(NewLine.getInstance());
	}

	private Text(Parcel in) {
		in.readList(mTexts, getClass().getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeList(mTexts);
	}

	public void add(String string) {
		mTexts.add(new FormatedText(string));
	}

	public void addNewLine(String string) {
		add(string);
		mTexts.add(NewLine.getInstance());
	}

	public List<FormatedText> getTexts() {
		return mTexts;
	}

	public static final Creator<Text> CREATOR = new Creator<Text>() {

		@Override
		public Text createFromParcel(Parcel source) {
			return new Text(source);
		}

		@Override
		public Text[] newArray(int size) {
			return new Text[size];
		}

	};

}
