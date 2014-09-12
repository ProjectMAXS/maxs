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

package org.projectmaxs.shared.module.messagecontent;

import org.projectmaxs.shared.global.messagecontent.Element;

import android.content.Context;
import android.content.res.Resources;

public class BooleanElement {

	/**
	 * Create a new boolean element.
	 * 
	 * The humanReadable string is presented the user in case this element will be presented a human
	 * (e.g. it's send as a message). The xmlName is used in case it's presented in a machine
	 * readable form. The isEnabled boolean is the actual payload of information that this element
	 * carries.
	 * 
	 * @param humanReadable
	 *            A string where '%1$s' is replaced with enabled or disabled
	 * @param xmlName
	 *            The name of the XML element that may be generated
	 * @param isEnabled
	 * @param context
	 * @return The element.
	 */
	public static Element enabled(String humanReadable, String xmlName, boolean isEnabled,
			Context context) {
		int id;
		Resources res = context.getResources();
		// We can't use the R.string.enabled here, since this code is shared across the
		// modules and R is a imported class from within a particular module at compile-time.
		if (isEnabled) {
			id = res.getIdentifier("enabled", "string", context.getPackageName());
		} else {
			id = res.getIdentifier("disabled", "string", context.getPackageName());
		}
		String state = res.getString(id);
		humanReadable = String.format(humanReadable, state);
		return new Element(xmlName, Boolean.toString(isEnabled), humanReadable);
	}

	public static Element trueOrFalse(String humanReadableTrue, String humanReadableFalse,
			String xmlName, boolean b) {
		String string;
		if (b) {
			string = humanReadableTrue;
		} else {
			string = humanReadableFalse;
		}
		return new Element(xmlName, Boolean.toString(b), string);
	}

	public static Element successOrUnsuccessfully(String what, String xmlName, boolean b) {
		String conditionText;
		if (b) {
			conditionText = "Successfully ";
		} else {
			conditionText = "Unsuccessfully ";
		}
		return new Element(xmlName, Boolean.toString(b), conditionText + what);
	}
}
