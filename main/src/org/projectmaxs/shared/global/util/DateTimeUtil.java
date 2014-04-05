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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class DateTimeUtil {

	private static final DateFormat HOURS_MINUTES_SECONDS = new SimpleDateFormat("HH:mm:ss");
	private static final DateFormat DATE_FORMAT_FULL = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Convert to "HH:mm:ss"
	 * 
	 * @param milliseconds
	 * @return A time string in the format "HH:mm:ss"
	 */
	public static final String shortFromUtc(long milliseconds) {
		Date date = dateFromUtc(milliseconds);
		synchronized (HOURS_MINUTES_SECONDS) {
			return HOURS_MINUTES_SECONDS.format(date);
		}
	}

	/**
	 * Convert to "yyyy-HH-dd HH:mm:ss" format from millisconds (epoch time)
	 * 
	 * @param milliseconds
	 * @return A date/time string in the format "yyyy-HH-dd HH:mm:ss"
	 */
	public static final String fullFromUtc(long milliseconds) {
		Date date = dateFromUtc(milliseconds);
		return toFullDate(date);
	}

	public static final Date dateFromUtc(long milliseconds) {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		cal.setTimeInMillis(milliseconds);
		return cal.getTime();
	}

	/**
	 * Convert a timestamp from the system timezone to "yyyy-mm-dd HH:mm:ss"
	 * 
	 * @param timestamp
	 * @return A date/time string in the format "yyyy-HH-dd HH:mm:ss"
	 */
	public static final String toFullDate(long timestamp) {
		Date date = new Date(timestamp);
		return toFullDate(date);
	}

	public static final String toFullDate(Date date) {
		// SimpleDateFormat is not synchronized
		synchronized (DATE_FORMAT_FULL) {
			return DATE_FORMAT_FULL.format(date);
		}
	}
}
