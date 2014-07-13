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

package org.projectmaxs.module.nfc.tech;

import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.shared.global.messagecontent.AbstractElement;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.module.messagecontent.TextElement;

import android.nfc.Tag;
import android.nfc.tech.NfcA;

public class NfcAHandler implements TechHandler {

	public static final String TECH = "android.nfc.tech.NfcA";

	@Override
	public String getTech() {
		return TECH;
	}

	@Override
	public Element handle(Tag tag) {
		NfcA nfca = NfcA.get(tag);
		List<AbstractElement> elements = new LinkedList<AbstractElement>();

		elements.add(TextElement.keyValueFrom("sak", "SAK/SEL_RES", Short.toString(nfca.getSak())));
		elements.add(TextElement.keyValueFrom("atqa", "ATQA/SENS_RES",
				SharedStringUtil.byteToHexString(nfca.getAtqa())));

		Element element = new Element("nfca_tech", "NFC-A (ISO 14443-3A) Technology Information");
		element.addChildElements(elements);
		return element;
	}
}
