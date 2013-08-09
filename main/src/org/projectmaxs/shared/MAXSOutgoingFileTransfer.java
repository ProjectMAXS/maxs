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

package org.projectmaxs.shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import android.os.Parcel;
import android.os.Parcelable;

public class MAXSOutgoingFileTransfer extends MAXSFileTransfer implements Parcelable {

	private final int mCmdId;

	public MAXSOutgoingFileTransfer(File file, String description, int cmdId) {
		if (!file.isFile()) throw new IllegalStateException("File '" + file + "' is not a file");

		mFilename = file.getName();
		mSize = file.length();
		mDescription = description;
		mCmdId = cmdId;
	}

	private MAXSOutgoingFileTransfer(Parcel in) {
		super(in);
		mCmdId = in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeInt(mCmdId);
	}

	public InputStream getInputStream() {
		return new FileInputStream(mPfd.getFileDescriptor());
	}

	public static final Creator<MAXSOutgoingFileTransfer> CREATOR = new Creator<MAXSOutgoingFileTransfer>() {

		@Override
		public MAXSOutgoingFileTransfer createFromParcel(Parcel source) {
			return new MAXSOutgoingFileTransfer(source);
		}

		@Override
		public MAXSOutgoingFileTransfer[] newArray(int size) {
			return new MAXSOutgoingFileTransfer[size];
		}
	};
}
