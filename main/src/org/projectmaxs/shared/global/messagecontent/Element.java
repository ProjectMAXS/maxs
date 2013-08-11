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

public class Element extends AbstractElement {

	private String mText;

	public Element(String name, String xmlName) {
		mName = name;
		mXMLName = xmlName;
	}

	public void setText(String text) {
		mText = text;
	}

	private Element(Parcel in) {
		mName = in.readString();
		mXMLName = in.readString();
		mText = in.readString();
		in.readList(mChildElements, null);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mName);
		dest.writeString(mXMLName);
		dest.writeString(mText);
		dest.writeList(mChildElements);
	}

	public StringBuilder getStringBuilder() {
		StringBuilder sb = new StringBuilder();
		sb.append(mName).append('\n');
		if (mText != null) sb.append(mText).append('\n');
		sb.append(getChildElementStringBuilder());

		return sb;
	}

	public static final Creator<Element> CREATOR = new Creator<Element>() {

		@Override
		public Element createFromParcel(Parcel source) {
			return new Element(source);
		}

		@Override
		public Element[] newArray(int size) {
			return new Element[size];
		}

	};

}
