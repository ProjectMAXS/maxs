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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.shared.global.messagecontent.AbstractElement;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.global.util.ParcelUtil;

import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {
	public static final int NO_ID = -1;

	private final List<AbstractElement> mElements = new LinkedList<AbstractElement>();
	private int mId = NO_ID;
	private boolean mSuccess = true;

	public Message() {}

	public Message(AbstractElement element) {
		add(element);
	}

	public Message(String string) {
		mElements.add(new Text(string, true));
	}

	public Message(String string, boolean success) {
		this(string);
		mSuccess = success;
	}

	public Message(String string, int id) {
		this(string);
		mId = id;
	}

	public Message(Collection<? extends AbstractElement> elements) {
		mElements.addAll(elements);
	}

	public Message(int id) {
		mId = id;
	}

	public void setId(int id) {
		mId = id;
	}

	public int getId() {
		return mId;
	}

	public void setSuccess(boolean success) {
		mSuccess = success;
	}

	public boolean isSuccess() {
		return mSuccess;
	}

	public Message add(AbstractElement element) {
		mElements.add(element);
		return this;
	}

	public Message addAll(Collection<? extends AbstractElement> elements) {
		mElements.addAll(elements);
		return this;
	}

	public Message add(String string, boolean newLine) {
		AbstractElement last = mElements.get(mElements.size() - 1);
		if (last instanceof Text) {
			Text lastText = (Text) last;
			if (newLine) {
				lastText.addNL(string);
			} else {
				lastText.add(string);
			}
		} else {
			mElements.add(new Text(string, newLine));
		}

		return this;
	}

	public Iterator<AbstractElement> getElementsIt() {
		return mElements.iterator();
	}

	private Message(Parcel in) {
		mSuccess = ParcelUtil.readBool(in);
		mId = in.readInt();
		in.readList(mElements, getClass().getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		ParcelUtil.writeBool(dest, mSuccess);
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
