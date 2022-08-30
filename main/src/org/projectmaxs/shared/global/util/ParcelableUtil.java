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

package org.projectmaxs.shared.global.util;

import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableUtil {

	private static final Log LOG = Log.getLog(ParcelableUtil.class);

	public static byte[] marshall(Parcelable parceable) {
		Parcel parcel = Parcel.obtain();
		parceable.writeToParcel(parcel, 0);
		byte[] bytes = parcel.marshall();
		parcel.recycle();
		return bytes;
	}

	public static Parcel unmarshall(byte[] bytes) {
		Parcel parcel = Parcel.obtain();
		parcel.unmarshall(bytes, 0, bytes.length);
		// This sh**t took me 2 hours to figure out, thanks to
		// http://stackoverflow.com/a/1678057/194894
		parcel.setDataPosition(0);
		return parcel;
	}

	public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
		return unmarshall(bytes, creator, false);
	}
	public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator, boolean ignoreExceptions) {
		Parcel parcel = unmarshall(bytes);
		T result = null;
		try {
			result = creator.createFromParcel(parcel);
		} catch (BadParcelableException badParcelableException) {
			if (ignoreExceptions) {
				LOG.d("createFromParcel() threw " + badParcelableException, badParcelableException);
			} else {
				throw badParcelableException;
			}
		} finally {
			parcel.recycle();
		}
		return result;
	}
}
