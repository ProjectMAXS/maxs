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

import android.text.TextUtils;

public class Log {

	private static LogSettings sLogSettings;
	private final String mLogTag;

	public static Log getLog(Class<?> c) {
		return new Log(shortClassName(c));
	}

	public static Log getLog() {
		StackTraceElement[] s = new RuntimeException().getStackTrace();
		return getLog(shortClassName(s[1].getClassName()));
	}

	public static Log getLog(String logTag) {
		return new Log(logTag);
	}

	private Log(String logTag) {
		this.mLogTag = logTag;
	}

	public void initialize(LogSettings settings) {
		sLogSettings = settings;
	}

	public void w(String msg) {
		android.util.Log.w(mLogTag, msg);
	}

	public void w(String msg, Exception e) {
		android.util.Log.w(mLogTag, msg, e);
	}

	public void e(String msg) {
		android.util.Log.e(mLogTag, msg);
	}

	public void e(String msg, Exception e) {
		android.util.Log.e(mLogTag, msg, e);
	}

	public void d(String msg) {
		if (sLogSettings != null && sLogSettings.debugLog()) {
			android.util.Log.d(mLogTag, msg);
		}
		else {
			android.util.Log.d(mLogTag, msg);
		}
	}

	static public abstract class LogSettings {
		public abstract boolean debugLog();
	}

	private static String shortClassName(Class<?> c) {
		String className = c.getName();
		return shortClassName(className);
	}

	private static String shortClassName(String className) {
		int index = TextUtils.lastIndexOf(className, '.');
		return className.substring(index + 1);
	}
}
