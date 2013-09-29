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

import java.util.Iterator;
import java.util.List;

import org.projectmaxs.shared.global.messagecontent.AbstractElement;
import org.projectmaxs.shared.global.messagecontent.CommandHelp;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.messagecontent.ContactNumber;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.messagecontent.Sms;
import org.projectmaxs.shared.global.messagecontent.Text;

public class HumanReadableString {

	private static String sMobile = "Mobile";
	private static String sHome = "Home";
	private static String sWork = "Work";
	private static String sUnkown = "Unkown";
	private static String sOther = "Other";

	public static void toSB(AbstractElement element, StringBuilder sb) {
		if (element instanceof Contact) {
			toSB((Contact) element, sb);
		} else if (element instanceof ContactNumber) {
			toSB((ContactNumber) element, sb);
		} else if (element instanceof Element) {
			toSB((Element) element, sb);
		} else if (element instanceof Sms) {
			toSB((Sms) element, sb);
		} else if (element instanceof Text) {
			toSB((Text) element, sb);
		} else if (element instanceof CommandHelp) {
			toSB((CommandHelp) element, sb);
		} else {
			throw new IllegalStateException("Unkown sublcass of AbstractElement");
		}
	}

	private static void toSB(Contact contact, StringBuilder sb) {
		sb.append(contact.getDisplayName());
		sb.append('\n');

		List<ContactNumber> numbers = contact.getNumbers();
		for (ContactNumber number : numbers)
			toSB(number, sb);
	}

	private static void toSB(ContactNumber contactNumber, StringBuilder sb) {
		String numberType;
		switch (contactNumber.getType()) {
		case MOBILE:
			numberType = sMobile;
			break;
		case HOME:
			numberType = sHome;
			break;
		case WORK:
			numberType = sWork;
			break;
		case OTHER:
			numberType = sOther;
			break;
		default:
			numberType = sUnkown;
			break;
		}
		sb.append(numberType);
		if (contactNumber.getLabel() != null) sb.append(" (" + contactNumber.getLabel() + ")");
		sb.append(": ");
		sb.append(contactNumber.getNumber());
		sb.append('\n');
	}

	private static void toSB(Element element, StringBuilder sb) {
		if (!element.isHumanReadable()) return;

		toSB(element.getHumanReadableName(), sb);

		Iterator<AbstractElement> it = element.getChildElementIterator();
		while (it.hasNext())
			toSB(it.next(), sb);
	}

	private static void toSB(Sms sms, StringBuilder sb) {
		sb.append(sms.getContact());
		sb.append(": ");
		sb.append(sms.getBody());
		sb.append('\n');
	}

	private static void toSB(Text text, StringBuilder sb) {
		List<org.projectmaxs.shared.global.messagecontent.FormatedText> texts = text.getTexts();
		for (org.projectmaxs.shared.global.messagecontent.FormatedText ft : texts)
			sb.append(ft.toString());
		// Don't append \n here. Text has it's own ways of adding them
	}

	private static void toSB(CommandHelp commandHelp, StringBuilder sb) {
		sb.append(commandHelp.mCommand);
		sb.append(' ');
		sb.append(commandHelp.mSubCommand);
		sb.append(' ');
		if (commandHelp.mArgType == ArgType.OTHER_STRING) {
			sb.append(commandHelp.mArgString);
		} else {
			sb.append(commandHelp.mArgType);
		}
		sb.append(' ');
		sb.append(commandHelp.mHelp);
		sb.append('\n');
	}

}
