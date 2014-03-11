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

package org.projectmaxs.transport.xmpp.util;

import java.util.List;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;
import org.jivesoftware.smackx.xhtmlim.XHTMLText;
import org.projectmaxs.shared.global.messagecontent.FormatedText;

public class XHTMLIMUtil {

	public static final Message addXHTMLIM(Message message, List<FormatedText> formatedText) {
		XHTMLText xhtmlText = new XHTMLText(null, null);
		for (FormatedText ft : formatedText) {
			if (FormatedText.isNewLine(ft)) {
				xhtmlText.appendBrTag();
				continue;
			}

			boolean bold = ft.isBold();
			boolean italic = ft.isItalic();
			// XEP-71 states in 8. Business Rules #7 that its RECOMMENDED to use structural elements
			// instead of style attributes. We follow this recommendation by using e.g. the Strong
			// tag for bold text instead of style='font-weight:bold' within a 'span' element.
			if (bold) xhtmlText.appendOpenStrongTag();
			if (italic) xhtmlText.appendOpenEmTag();
			xhtmlText.append(ft.toString());
			if (italic) xhtmlText.appendCloseEmTag();
			if (bold) xhtmlText.appendCloseStrongTag();
		}
		XHTMLManager.addBody(message, xhtmlText.toString());
		return message;
	}
}
