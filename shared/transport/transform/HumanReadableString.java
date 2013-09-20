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

	public static StringBuilder toSB(AbstractElement element) {
		StringBuilder sb;

		if (element instanceof Contact) {
			sb = toSB((Contact) element);
		} else if (element instanceof ContactNumber) {
			sb = toSB((ContactNumber) element);
		} else if (element instanceof Element) {
			sb = toSB((Element) element);
		} else if (element instanceof Sms) {
			sb = toSB((Sms) element);
		} else if (element instanceof Text) {
			sb = toSB((Text) element);
		} else {
			throw new IllegalStateException("Unkown sublcass of AbstractElement");
		}
		return sb;
	}

	private static StringBuilder toSB(Contact contact) {
		StringBuilder sb = new StringBuilder();
		sb.append(contact.getDisplayName());
		sb.append('\n');

		List<ContactNumber> numbers = contact.getNumbers();
		for (ContactNumber number : numbers) {
			sb.append(toSB(number));
		}

		return sb;
	}

	private static StringBuilder toSB(ContactNumber contactNumber) {
		StringBuilder sb = new StringBuilder();
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
		return sb;
	}

	private static StringBuilder toSB(Element element) {
		if (!element.isHumanReadable()) return new StringBuilder(0);

		StringBuilder sb = new StringBuilder();
		sb.append(element.getHumanReadableName());
		sb.append('\n');

		Iterator<AbstractElement> it = element.getChildElementIterator();
		while (it.hasNext())
			sb.append(toSB(it.next()));
		return sb;
	}

	private static StringBuilder toSB(Sms sms) {
		StringBuilder sb = new StringBuilder();
		sb.append(sms.getContact());
		sb.append(": ");
		sb.append(sms.getBody());
		return sb;
	}

	private static StringBuilder toSB(Text text) {
		StringBuilder sb = new StringBuilder();
		List<org.projectmaxs.shared.global.messagecontent.FormatedText> texts = text.getTexts();
		for (org.projectmaxs.shared.global.messagecontent.FormatedText ft : texts) {
			sb.append(ft.toString());
		}
		return sb;
	}

}
