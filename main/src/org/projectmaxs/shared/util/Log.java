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

public class Log {

	static private String sLogTag;
	static private LogSettings sLogSettings;

	static public void initialize(String logTag, LogSettings settings) {
		sLogTag = logTag;
		sLogSettings = settings;
	}

	public static void w(String msg) {
		android.util.Log.i(sLogTag, msg);
	}

	public static void w(String msg, Exception e) {
		android.util.Log.w(sLogTag, msg, e);
	}

	public static void e(String msg) {
		android.util.Log.e(sLogTag, msg);
	}

	public static void e(String msg, Exception e) {
		android.util.Log.e(sLogTag, msg, e);
	}

	public static void d(String msg) {
		if (sLogSettings.debugLog()) android.util.Log.d(sLogTag, msg);
	}

	static public abstract class LogSettings {
		public abstract boolean debugLog();
	}
}
