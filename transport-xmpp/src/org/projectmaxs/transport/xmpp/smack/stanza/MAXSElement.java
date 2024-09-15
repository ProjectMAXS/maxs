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

package org.projectmaxs.transport.xmpp.smack.stanza;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StanzaBuilder;
import org.jivesoftware.smack.packet.XmlEnvironment;

import javax.xml.namespace.QName;

/**
 * A MAXS extension element, used to indicate messages send by MAXS. Those are either responses to a
 * MAXS command or broadcasts send by MAXS. The element is useful to identity messages send by MAXS
 * in order to prevent endless loops of messages being exchanged between one or multiple MAXS
 * instances.
 * <p>
 * This happens for example, if a user used the same JID as master JID and as MAXS's JID. Now it's
 * possible that MAXS send and unknown command message to itself, causing another unknown command
 * message. And so on.
 * </p>
 */
public class MAXSElement implements ExtensionElement {

	public static final MAXSElement INSTANCE = new MAXSElement();

	public static final String ELEMENT = "maxs";
	public static final String NAMESPACE = "https://projectmaxs.org";

	public static final QName QNAME = new QName(NAMESPACE, ELEMENT);

	private MAXSElement() {}

	@Override
	public String getElementName() {
		return ELEMENT;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML(XmlEnvironment xmlEnvironment) {
		return '<' + ELEMENT + " xmlns='" + NAMESPACE + "'/>";
	}

	public static MAXSElement from(Stanza stanza) {
		return stanza.getExtension(MAXSElement.class);
	}

	public static boolean foundIn(Stanza stanza) {
		return stanza.hasExtension(QNAME);
	}

	public static void addTo(StanzaBuilder stanza) {
		stanza.addExtension(INSTANCE);
	}
}
