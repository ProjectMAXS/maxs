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

package org.projectmaxs.module.nfc;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.projectmaxs.module.nfc.tech.NfcAHandler;
import org.projectmaxs.module.nfc.tech.TechHandler;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.AbstractElement;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.module.MainUtil;
import org.projectmaxs.shared.module.messagecontent.TextElement;

import android.app.IntentService;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

public class NfcDiscoveredService extends IntentService {

	public static final String CAUSING_INTENT_EXTRA = "CAUSING_INTENT_EXTRA";

	private static final Log LOG = Log.getLog();
	private static final Map<String, TechHandler> techHandlers = new HashMap<String, TechHandler>();

	static {
		registerHandler(NfcAHandler.class);
	}

	public NfcDiscoveredService() {
		super("NfcDiscoveredService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Intent causingIntent = intent.getParcelableExtra(CAUSING_INTENT_EXTRA);
		String action = causingIntent.getAction();
		LOG.d("causingIntent action=" + action);
		Tag tag = causingIntent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		Element element = new Element("nfc_tag", "NFC Tag");
		List<AbstractElement> elements = new LinkedList<AbstractElement>();
		element.addChildElements(elements);

		elements.add(TextElement.keyValueFrom("nfc_tag_id", "Tag ID",
				SharedStringUtil.byteToHexString(tag.getId())));
		Element techListElement = new Element("tech_list", "Technology List");
		for (String tech : tag.getTechList()) {
			// Add 'tech' as xmlName *and* humanReadableName
			techListElement.addChildElement(new Element(tech, tech));
		}
		elements.add(techListElement);

		for (String tech : tag.getTechList()) {
			TechHandler techHandler = techHandlers.get(tech);
			if (techHandler == null) {
				continue;
			}
			elements.add(techHandler.handle(tag));
		}
		MainUtil.send(new Message(elements), this);
	}

	private static synchronized <H extends TechHandler> void registerHandler(Class<H> handlerClass) {
		H handler;
		try {
			Constructor<H> constructor = handlerClass.getConstructor();
			handler = constructor.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		techHandlers.put(handler.getTech(), handler);
	}
}
