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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class AbstractElement implements Parcelable {

	protected final List<AbstractElement> mChildElements = new LinkedList<AbstractElement>();
	protected String mName;
	protected String mXMLName;

	@Override
	public abstract int describeContents();

	/**
	 * Remember to write also mChildElements
	 */
	@Override
	public abstract void writeToParcel(Parcel dest, int flags);

	public abstract StringBuilder getStringBuilder();

	public String getRawContent() {
		return getStringBuilder().toString();
	}

	public void addChildElement(AbstractElement element) {
		mChildElements.add(element);
	}

	public Iterator<AbstractElement> getChildElementIterator() {
		return mChildElements.iterator();
	}

	protected StringBuilder getChildElementStringBuilder() {
		StringBuilder sb = new StringBuilder();
		Iterator<AbstractElement> it = getChildElementIterator();
		while (it.hasNext())
			sb.append(it.next().getStringBuilder()).append('\n');
		return sb;
	}
}
