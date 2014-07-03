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

	public Text() {}

	public Text(CharSequence charSequence) {
		this(charSequence, true);
	}

	public Text(CharSequence charSequence, boolean newLine) {
		addWithNewLines(charSequence);
		// Only add a NewLine if it's required and if it's not already there
		if (newLine && mTexts.get(mTexts.size() - 1) != NewLine.getInstance())
			mTexts.add(NewLine.getInstance());
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

	public Text add(FormatedText formatedText) {
		mTexts.add(formatedText);
		return this;
	}

	public Text add(CharSequence charSequence) {
		mTexts.add(new FormatedText(charSequence));
		return this;
	}

	public Text addWithNewLines(CharSequence charSequence) {
		// Transform all new line characters to NewLine. This is necessary since certain formats,
		// like XHTML-IM require all new lines to be replaced with <br>
		StringBuilder sb = new StringBuilder(512);
		for (int i = 0; i < charSequence.length(); i++) {
			char c = charSequence.charAt(i);
			if (c == '\n') {
				addNL(sb.toString());
				sb.delete(0, sb.length());
			} else {
				sb.append(c);
			}
		}
		// Ensure that we also add the list line, even if it's not '\n' terminated
		if (sb.length() > 0) mTexts.add(new FormatedText(sb.toString()));
		return this;
	}

	public Text addNL(CharSequence charSequence) {
		add(charSequence);
		mTexts.add(NewLine.getInstance());
		return this;
	}

	public Text addBold(CharSequence charSequence) {
		mTexts.add(new FormatedText(charSequence).makeBold());
		return this;
	}

	public Text addBoldNL(CharSequence charSequence) {
		addBold(charSequence);
		mTexts.add(NewLine.getInstance());
		return this;
	}

	public Text addItalic(CharSequence charSequence) {
		mTexts.add(new FormatedText(charSequence).makeItalic());
		return this;
	}

	public Text addItalicNL(CharSequence charSequence) {
		addItalic(charSequence);
		mTexts.add(NewLine.getInstance());
		return this;
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

	public static Text create() {
		return new Text();
	}

	public static Text createBoldNL(CharSequence charSequence) {
		Text text = new Text();
		text.addBoldNL(charSequence);
		return text;
	}
}
