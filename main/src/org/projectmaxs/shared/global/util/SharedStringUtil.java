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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.projectmaxs.shared.global.messagecontent.Contact;

public class SharedStringUtil {

	private static final SimpleDateFormat DATE_FORMAT_FULL = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public static String getSubstringAfter(String s, char c) {
		return s.substring(s.lastIndexOf(c) + 1).trim();
	}

	public static Set<String> stringToSet(String string) {
		Set<String> res = new HashSet<String>();
		if (string != null && !string.equals("")) {
			res.addAll(Arrays.asList(string.split(" ")));
		}
		return res;
	}

	public static String setToString(Set<String> set) {
		StringBuilder sb = new StringBuilder();
		for (String s : set) {
			sb.append(s);
			sb.append(" ");
		}
		return sb.toString();
	}

	public static String ipIntToString(int ip) {
		// @formatter:off
		return String.format("%d.%d.%d.%d",
				(ip & 0xff),
				(ip >> 8 & 0xff),
				(ip >> 16 & 0xff),
				(ip >> 24 & 0xff));
		// @formatter:on
	}

	public static String[] toStringArray(int[] intArray) {
		String[] res = new String[intArray.length];
		for (int i = 0; i < intArray.length; i++) {
			res[i] = Integer.toString(intArray[i]);
		}
		return res;
	}

	public static String shorten(String string, int maxSize) {
		String res;
		if (string.length() < maxSize) {
			res = string;
		} else {
			res = string.substring(0, maxSize) + "...";
		}
		return res;
	}

	/**
	 * Pretty print contact information. This either prints just contactString or, if contact is not
	 * null, the display name of the contact with the contactString in parentheses.
	 * 
	 * @param contactString
	 * @param contact
	 *            , optional
	 * @return
	 */
	public static final String prettyPrint(String contactString, Contact contact) {
		return contact != null ? contact.getDisplayName() + " (" + contactString + ")"
				: contactString;
	}

	public static final boolean isInteger(String s) {
		return isInteger(s, 10);
	}

	public static final boolean isInteger(String s, int radix) {
		if (s.isEmpty()) return false;
		for (int i = 0; i < s.length(); i++) {
			if (i == 0 && s.charAt(i) == '-') {
				if (s.length() == 1)
					return false;
				else
					continue;
			}
			if (Character.digit(s.charAt(i), radix) < 0) return false;
		}
		return true;
	}

	/**
	 * Convert a long to "yyyy-mm-dd HH:mm:ss"
	 * 
	 * @param timestamp
	 * @return
	 */
	public static final String toFullDate(long timestamp) {
		Date date = new Date(timestamp);
		// SimpleDateFormat is not synchronized
		synchronized (DATE_FORMAT_FULL) {
			return DATE_FORMAT_FULL.format(date);
		}
	}

	public static final String humandReadableByteCount(long bytes) {
		if (bytes < 1024) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(1024));
		String pre = "KMGTPE".charAt(exp - 1) + "iB";
		return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
	}

	public static int countMatches(String haystack, char c) {
		int count = 0;
		for (int i = 0; i < haystack.length(); i++) {
			if (haystack.charAt(i) == c) count++;
		}
		return count;
	}

	/**
	 * Returns the substring after the last dot ('.').
	 * "bar" = substringAfterLastDot("my.foo.bar");
	 * 
	 * @param string
	 * @return
	 */
	public static String substringAfterLastDot(String string) {
		return getSubstringAfter(string, '.');
	}
}
