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
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log.DebugLogSettings;
import org.projectmaxs.shared.global.util.SharedStringUtil;

import android.util.Log;

@SuppressWarnings("deprecation")
public class JULHandler extends Handler {

	private static final String CLASS_NAME = JULHandler.class.getName();

	/**
	 * The global LogManager configuration.
	 * 
	 * This configures:
	 * - JULHandler as the default handler for all log messages
	 * - A default log level FINEST (300). Meaning that log messages of a level 300 or higher a
	 * logged
	 */
	private static final InputStream LOG_MANAGER_CONFIG = new StringBufferInputStream(
// @formatter:off
"handlers = " + CLASS_NAME + '\n' +
".level = FINEST"
);
// @formatter:on

	private static final int FINE_INT = Level.FINE.intValue();
	private static final int INFO_INT = Level.INFO.intValue();
	private static final int WARN_INT = Level.WARNING.intValue();
	private static final int SEVE_INT = Level.SEVERE.intValue();

	private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

	private static final Set<String> NO_LOG_PKGS = new HashSet<String>();

	private static final Formatter FORMATTER = new Formatter() {
		@Override
		public String format(LogRecord logRecord) {
			Throwable thrown = logRecord.getThrown();
			if (thrown != null) {
				String stacktrace = Log.getStackTraceString(thrown);
				return logRecord.getMessage() + ' ' + stacktrace;
			} else {
				return logRecord.getMessage();
			}
		}
	};

	private static DebugLogSettings sDebugLogSettings;

	static {
		boolean initialized = false;
		try {
			LogManager.getLogManager().readConfiguration(LOG_MANAGER_CONFIG);
			initialized = true;
		} catch (IOException e) {
			Log.e("JULHandler", "Can not initialize configuration", e);
		}
		if (initialized) LOGGER.info("Initialzied java.util.logging logger");
	}

	public static void init(DebugLogSettings debugLogSettings) {
		sDebugLogSettings = debugLogSettings;
	}

	public static void addNoLogPkg(String pkg) {
		NO_LOG_PKGS.add(pkg);
	}

	public static void removeNoLogPkg(String pkg) {
		NO_LOG_PKGS.remove(pkg);
	}

	public JULHandler() {
		setFormatter(FORMATTER);
	}

	@Override
	public void close() {}

	@Override
	public void flush() {}

	@Override
	public boolean isLoggable(LogRecord record) {
		final String sourceClass = record.getSourceClassName();
		if (sourceClass != null) {
			for (String noLogPkg : NO_LOG_PKGS) {
				if (sourceClass.startsWith(noLogPkg)) {
					return false;
				}
			}
		}

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

		final int priority = getAndroidPriority(record.getLevel());
		final String tag = GlobalConstants.MAXS + '/'
				+ SharedStringUtil.substringAfterLastDot(record.getSourceClassName());
		final String msg = getFormatter().format(record);

		Log.println(priority, tag, msg);
	}

	private static int getAndroidPriority(Level level) {
		int value = level.intValue();
		if (value >= SEVE_INT) {
			return Log.ERROR;
		} else if (value >= WARN_INT) {
			return Log.WARN;
		} else if (value >= INFO_INT) {
			return Log.INFO;
		} else {
			return Log.DEBUG;
		}
	}

	private static final UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = new UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			LOGGER.log(Level.SEVERE, "Uncaught exception in " + thread, ex);
		}
	};

	public static void setAsDefaultUncaughtExceptionHandler() {
		UncaughtExceptionHandler currentUncaughtExceptionHandler = Thread
				.getDefaultUncaughtExceptionHandler();
		if (currentUncaughtExceptionHandler != UNCAUGHT_EXCEPTION_HANDLER) {
			Thread.setDefaultUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
		}
	}
}
