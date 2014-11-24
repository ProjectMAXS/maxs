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
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.messagecontent.ContactNumber;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.messagecontent.FormatedText;
import org.projectmaxs.shared.global.messagecontent.NewLine;
import org.projectmaxs.shared.global.messagecontent.Sms;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.global.util.DateTimeUtil;

public class FormatedTextTransformator {

	protected static void toFormatedText(AbstractElement element, List<FormatedText> ft) {
		if (element instanceof Contact) {
			toFormatedText((Contact) element, ft);
		} else if (element instanceof ContactNumber) {
			toFormatedText((ContactNumber) element, ft);
		} else if (element instanceof Element) {
			toFormatedText((Element) element, ft);
		} else if (element instanceof Sms) {
			toFormatedText((Sms) element, ft);
		} else if (element instanceof Text) {
			toFormatedText((Text) element, ft);
		} else if (element instanceof CommandHelp) {
			toFormatedText((CommandHelp) element, ft);
		} else {
			throw new IllegalStateException("Unknown sublcass of AbstractElement");
		}
	}

	private static void toFormatedText(Contact contact, List<FormatedText> ft) {
		ft.add(FormatedText.bold(contact.getDisplayName()));
		ft.add(NewLine.getInstance());

		for (ContactNumber number : contact.getNumbers())
			toFormatedText(number, ft);
	}

	private static void toFormatedText(ContactNumber contactNumber, List<FormatedText> ft) {
		ft.add(FormatedText.italic(TypeTransformator.fromNumberType(contactNumber.getType())));
		if (contactNumber.getLabel() != null)
			ft.add(FormatedText.from(" (" + contactNumber.getLabel() + ")"));
		ft.add(FormatedText.from(": " + contactNumber.getNumber()));
		ft.add(NewLine.getInstance());
	}

	private static void toFormatedText(Element element, List<FormatedText> ft) {
		if (!element.isHumanReadable()) return;

		toFormatedText(element.getHumanReadableName(), ft);

		Iterator<AbstractElement> it = element.getChildElementIterator();
		while (it.hasNext())
			toFormatedText(it.next(), ft);
	}

	private static void toFormatedText(Sms sms, List<FormatedText> ft) {
		ft.add(FormatedText.from(TypeTransformator.fromSMSType(sms.getType())));
		ft.add(FormatedText.SINGLE_SPACE);

		ft.add(FormatedText.bold(sms.getContact()));
		ft.add(FormatedText.SINGLE_SPACE);
		ft.add(FormatedText.italic(DateTimeUtil.toFullDate(sms.getDate())));
		ft.add(FormatedText.from(": "));
		ft.add(FormatedText.from(sms.getBody()));
		ft.add(NewLine.getInstance());
	}

	private static void toFormatedText(Text text, List<FormatedText> ft) {
		ft.addAll(text.getTexts());
	}

	private static void toFormatedText(CommandHelp commandHelp, List<FormatedText> ft) {
		StringBuilder sb = new StringBuilder();
		sb.append(commandHelp.mCommand);
		sb.append(' ');
		sb.append(commandHelp.mSubCommand);
		sb.append(TypeTransformator.toCommandArg(commandHelp));
		ft.add(FormatedText.bold(sb));

		sb = new StringBuilder();
		sb.append(" - ");
		sb.append(commandHelp.mHelp);
		ft.add(FormatedText.from(sb));
		ft.add(NewLine.getInstance());
	}
}
