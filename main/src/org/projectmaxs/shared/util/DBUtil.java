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

package org.projectmaxs.shared.util;

import android.database.Cursor;

public class DBUtil {

	public final static long getLong(Cursor c, String col) {
		return c.getLong(c.getColumnIndexOrThrow(col));
	}

	public final static int getInt(Cursor c, String col) {
		return c.getInt(c.getColumnIndexOrThrow(col));
	}

	public final static String getString(Cursor c, String col) {
		return c.getString(c.getColumnIndexOrThrow(col));
	}

	public final static boolean getBoolean(Cursor c, String col) {
		return getInt(c, col) == 1;
	}

}
