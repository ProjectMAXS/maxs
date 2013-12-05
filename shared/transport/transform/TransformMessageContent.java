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

package org.projectmaxs.shared.transport.transform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.AbstractElement;
import org.projectmaxs.shared.global.messagecontent.FormatedText;

public class TransformMessageContent {

	public static String toString(Message message) {
		StringBuilder sb = new StringBuilder();
		Iterator<AbstractElement> it = message.getElementsIt();
		while (it.hasNext())
			HumanReadableString.toSB(it.next(), sb);
		// Remove the last newline of the message
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') sb.setLength(sb.length() - 1);

		return sb.toString();
	}

	public static List<FormatedText> toFormatedText(Message message) {
		List<FormatedText> res = new ArrayList<FormatedText>();
		Iterator<AbstractElement> it = message.getElementsIt();
		while (it.hasNext())
			FormatedTextTransformator.toFormatedText(it.next(), res);

		return res;
	}

	public static String toXML(Message message) {
		StringBuilder sb = new StringBuilder();
		Iterator<AbstractElement> it = message.getElementsIt();
		while (it.hasNext())
			sb.append(XML.toSB(it.next()));

		return sb.toString();
	}

}
