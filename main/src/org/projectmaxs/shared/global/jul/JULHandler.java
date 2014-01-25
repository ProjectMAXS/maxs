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

package org.projectmaxs.shared.global.jul;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.projectmaxs.shared.global.util.Log.DebugLogSettings;

import android.util.Log;

@SuppressWarnings("deprecation")
public class JULHandler extends Handler {

	private static final String CLASS_NAME = JULHandler.class.getName();
	private static final InputStream LOG_MANAGER_CONFIG = new StringBufferInputStream("handlers = "
			+ CLASS_NAME);

	private static final int FINE_INT = Level.FINE.intValue();
	private static final int INFO_INT = Level.INFO.intValue();
	private static final int WARN_INT = Level.WARNING.intValue();
	private static final int SEVE_INT = Level.SEVERE.intValue();

	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private static DebugLogSettings sDebugLogSettings;

	public static synchronized void init(DebugLogSettings debugLogSettings) {
		if (sDebugLogSettings != null) return;

		sDebugLogSettings = debugLogSettings;
		try {
			LogManager.getLogManager().readConfiguration(LOG_MANAGER_CONFIG);
		} catch (IOException e) {
			Log.e("JULHandler", "Can not initialize configuration", e);
			return;
		}
		LOGGER.info("Initialzied java.util.logging logger");
	}

	@Override
	public void close() {}

	@Override
	public void flush() {}

	@Override
	public boolean isLoggable(LogRecord record) {
		final boolean debugLog = sDebugLogSettings == null ? true : sDebugLogSettings
				.isDebugLogEnabled();

		if (record.getLevel().intValue() <= FINE_INT) {
			return debugLog;
		}
		return true;
	}

	@Override
	public void publish(LogRecord record) {
		if (!isLoggable(record)) return;

		final int level = record.getLevel().intValue();
		final String tag = record.getSourceClassName();
		final String msg = record.getMessage();
		final Throwable tr = record.getThrown();

		if (level <= FINE_INT) {
			if (tr != null) {
				Log.d(tag, msg, tr);
			} else {
				Log.d(tag, msg);
			}
		} else if (level <= INFO_INT) {
			if (tr != null) {
				Log.i(tag, msg, tr);
			} else {
				Log.i(tag, msg);
			}
		} else if (level <= WARN_INT) {
			if (tr != null) {
				Log.w(tag, msg, tr);
			} else {
				Log.w(tag, msg);
			}
		} else if (level <= SEVE_INT) {
			if (tr != null) {
				Log.e(tag, msg, tr);
			} else {
				Log.e(tag, msg);
			}
		}
	}
}
