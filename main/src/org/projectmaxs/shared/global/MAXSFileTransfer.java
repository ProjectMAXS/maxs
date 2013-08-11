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

import java.io.FileInputStream;
import java.io.InputStream;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

public abstract class MAXSFileTransfer implements Parcelable {

	protected String mFilename;
	protected long mSize;
	protected String mDescription;
	protected ParcelFileDescriptor mPfd;
	protected String mInvolvedJid;

	public MAXSFileTransfer() {
	}

	public String getFilename() {
		return mFilename;
	}

	public long getSize() {
		return mSize;
	}

	public String getDescription() {
		return mDescription;
	}

	public InputStream getInputStream() {
		return new FileInputStream(mPfd.getFileDescriptor());
	}

	public MAXSFileTransfer(String filename, long size, String description, ParcelFileDescriptor pdf, String invovledJid) {
		mFilename = filename;
		mSize = size;
		mDescription = description;
		mPfd = pdf;
		mInvolvedJid = invovledJid;
	}

	protected MAXSFileTransfer(Parcel in) {
		mFilename = in.readString();
		mSize = in.readLong();
		mDescription = in.readString();
		mPfd = in.readFileDescriptor();
		mInvolvedJid = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mFilename);
		dest.writeLong(mSize);
		dest.writeString(mDescription);
		mPfd.writeToParcel(dest, flags);
		dest.writeString(mInvolvedJid);
	}
}
