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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

public class SharedStringUtil {

	private static final String POSITIVE_INTEGER_REGEX = "[0-9]+";
	private static final String INTEGER_REGEX = "-?" + POSITIVE_INTEGER_REGEX;

	private static final Pattern POSITIVE_INTEGER_PATTERN = Pattern.compile(POSITIVE_INTEGER_REGEX);
	private static final Pattern INTEGER_PATTERN = Pattern.compile(INTEGER_REGEX);

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

	public static StringBuilder listCollection(Collection<? extends CharSequence> collection) {
		StringBuilder sb = new StringBuilder();
		Iterator<? extends CharSequence> it = collection.iterator();
		while (it.hasNext()) {
			CharSequence entry = it.next();
			sb.append(entry.toString());
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		return sb;
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

	public static final boolean isPositiveInteger(String s) {
		return POSITIVE_INTEGER_PATTERN.matcher(s).matches();
	}

	public static final boolean isInteger(String s) {
		return INTEGER_PATTERN.matcher(s).matches();
	}

	public static final String humandReadableByteCount(long bytes) {
		if (bytes < 1024) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(1024));
		String pre = "KMGTPE".charAt(exp - 1) + "iB";
		return String.format("%.1f %s", bytes / Math.pow(1024, exp), pre);
	}

	public static final String humanReadableMilliseconds(long milliseconds) {
		return milliseconds + "ms";
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
	 * @return The substring after the last dot.
	 */
	public static String substringAfterLastDot(String string) {
		return getSubstringAfter(string, '.');
	}

	public static boolean isNullOrEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	public static String byteToHex(byte b) {
		return String.format("x%02X", b);
	}

	public static String byteToHexString(byte[] byteArray) {
		StringBuilder sb = new StringBuilder(byteArray.length * 3);
		for (byte b : byteArray) {
			sb.append(byteToHex(b));
		}
		return sb.toString();
	}
}
