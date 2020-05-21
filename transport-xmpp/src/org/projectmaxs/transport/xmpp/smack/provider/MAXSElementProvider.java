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

package org.projectmaxs.transport.xmpp.smack.provider;

import org.jivesoftware.smack.packet.XmlEnvironment;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.xml.XmlPullParser;
import org.projectmaxs.transport.xmpp.smack.stanza.MAXSElement;

public class MAXSElementProvider extends ExtensionElementProvider<MAXSElement> {

	public static final MAXSElementProvider INSTANCE = new MAXSElementProvider();

	private MAXSElementProvider() {}

	@Override
	public MAXSElement parse(XmlPullParser parser, int initialDepth, XmlEnvironment xmlEnvironment) {
		return MAXSElement.INSTANCE;
	}

	public static void setup() {
		ProviderManager.addExtensionProvider(MAXSElement.ELEMENT, MAXSElement.NAMESPACE, INSTANCE);
	}
}
