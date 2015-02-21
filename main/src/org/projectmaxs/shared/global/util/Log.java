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

import org.projectmaxs.shared.global.GlobalConstants;

public class Log {

	private static DebugLogSettings sDebugLogSettings;
	private final String mLogTag;

	public static Log getLog(Class<?> c) {
		return new Log(shortClassName(c));
	}

	public static Log getLog() {
		StackTraceElement[] s = new RuntimeException().getStackTrace();
		return getLog(SharedStringUtil.substringAfterLastDot(s[1].getClassName()));
	}

	public static Log getLog(String logTag) {
		return new Log(logTag);
	}

	private Log(String logTag) {
		this.mLogTag = GlobalConstants.MAXS + '/' + logTag;
	}

	public void initialize(DebugLogSettings settings) {
		sDebugLogSettings = settings;
	}

	public void i(String msg) {
		android.util.Log.i(mLogTag, msg);
	}

	public void i(String msg, Throwable tr) {
		android.util.Log.i(mLogTag, msg, tr);
	}

	public void w(String msg) {
		android.util.Log.w(mLogTag, msg);
	}

	public void w(String msg, Throwable tr) {
		android.util.Log.w(mLogTag, msg, tr);
	}

	public void e(String msg) {
		android.util.Log.e(mLogTag, msg);
	}

	public void e(String msg, Throwable tr) {
		android.util.Log.e(mLogTag, msg, tr);
	}

	public void d(CharSequence msg) {
		if (isDebugLogEnabled()) {
			android.util.Log.d(mLogTag, msg.toString());
		}
	}

	public void d(CharSequence msg, Throwable tr) {
		if (isDebugLogEnabled()) {
			android.util.Log.d(mLogTag, msg.toString(), tr);
		}
	}

	public boolean isDebugLogEnabled() {
		return sDebugLogSettings == null || sDebugLogSettings.isDebugLogEnabled();
	}

	public static interface DebugLogSettings {
		public boolean isDebugLogEnabled();
	}

	private static String shortClassName(Class<?> c) {
		String className = c.getName();
		return SharedStringUtil.substringAfterLastDot(className);
	}
}
