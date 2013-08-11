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

package org.projectmaxs.shared.global;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.shared.global.messagecontent.AbstractElement;
import org.projectmaxs.shared.global.messagecontent.Text;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {
	public static final int NO_ID = -1;

	private final List<AbstractElement> mElements = new LinkedList<AbstractElement>();
	private int mId;

	public Message(String string) {
		this(string, NO_ID);
	}

	public Message(String string, int id) {
		mId = id;
		mElements.add(new Text(string));
	}

	public void setId(int id) {
		mId = id;
	}

	public int getId() {
		return mId;
	}

	public Message add(String string) {
		AbstractElement last = mElements.get(mElements.size() - 1);
		if (last instanceof Text) {
			((Text) last).add(string);
		}
		else {
			mElements.add(new Text(string));
		}

		return this;
	}

	public String getRawContent() {
		StringBuilder sb = new StringBuilder();
		Iterator<AbstractElement> it = mElements.iterator();
		while (it.hasNext())
			sb.append(it.next().getStringBuilder());
		return sb.toString();
	}

	private Message(Parcel in) {
		mId = in.readInt();
		in.readList(mElements, getClass().getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeList(mElements);
	}

	public static final Creator<Message> CREATOR = new Creator<Message>() {

		@Override
		public Message createFromParcel(Parcel source) {
			return new Message(source);
		}

		@Override
		public Message[] newArray(int size) {
			return new Message[size];
		}

	};

}
