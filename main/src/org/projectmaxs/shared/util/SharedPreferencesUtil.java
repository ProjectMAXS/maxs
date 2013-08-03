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

import java.io.File;
import java.util.Map;
import java.util.Set;

import android.content.SharedPreferences;

public class SharedPreferencesUtil {

	private static final int BOOLEAN = 1;
	private static final int FLOAT = 2;
	private static final int INT = 3;
	private static final int LONG = 4;
	private static final int STRING = 5;

	// We don't support StringSet

	/**
	 * sharedPreferences must be API 8 compatible. That is, it must not contain
	 * StringSets.
	 * 
	 * @param sharedPreferences
	 * @param outDirectory
	 * @param doNotExport
	 *            a set of keys that should not get exported (passwords, etc.)
	 */
	public static void exportToFile(SharedPreferences sharedPreferences, File outFile, Set<String> doNotExport) {
		for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
			String key = entry.getKey();
			if (doNotExport.contains(key)) continue;

			Object value = entry.getValue();
			String valueType = value.getClass().getSimpleName();
		}
	}

	/**
	 * 
	 * @param sharedPreferences
	 * @param inFile
	 */
	public static void importFromFile(SharedPreferences sharedPreferences, File inFile) {

	}
}
