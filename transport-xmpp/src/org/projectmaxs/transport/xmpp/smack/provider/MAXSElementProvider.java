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

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.projectmaxs.transport.xmpp.smack.stanza.MAXSElement;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class MAXSElementProvider extends ExtensionElementProvider<MAXSElement> {

	public static final MAXSElementProvider INSTANCE = new MAXSElementProvider();

	private MAXSElementProvider() {}

	@Override
	public MAXSElement parse(XmlPullParser parser, int initialDepth)
			throws XmlPullParserException, IOException, SmackException {
		return MAXSElement.INSTANCE;
	}

	public static void setup() {
		ProviderManager.addExtensionProvider(MAXSElement.ELEMENT, MAXSElement.NAMESPACE, INSTANCE);
	}
}
