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

package org.projectmaxs.transport.xmpp;

import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.util.Constants;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPService;

import android.app.IntentService;
import android.content.Intent;

public class XmppIntentService extends IntentService {

	private static Log LOG = Log.getLog();

	private XMPPService mXMPPService;
	private Settings mSettings;

	public XmppIntentService() {
		super("XMPP Intent Service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (mSettings == null) mSettings = Settings.getInstance(this);
		if (!mSettings.isXmppIntentEnabled()) {
			LOG.d("XMPP intent not enabled");
			return;
		}
		final String sharedToken = mSettings.getXmppIntentSharedToken();
		if (sharedToken == null) {
			LOG.i("XMPP intent shared token not set (or XMPP intent disabled)");
			return;
		}
		final String givenSharedToken = intent.getStringExtra(Constants.PACKAGE + ".TOKEN");
		if (!sharedToken.equals(givenSharedToken)) {
			LOG.w("Given shared token '" + givenSharedToken + "' does not match shared token '"
					+ sharedToken + "'");
			return;
		}

		if (mXMPPService == null) mXMPPService = XMPPService.getInstance(this);
		final String action = intent.getAction();
		switch (action) {
		case Constants.PACKAGE + ".SEND_XMPP_MESSAGE":
			String to = intent.getStringExtra(Constants.PACKAGE + ".TO");
			if (to == null || to.isEmpty()) {
				LOG.w("TO extra not set or empty");
				return;
			}
			String body = intent.getStringExtra(Constants.PACKAGE + ".BODY");
			if (body == null || body.isEmpty()) {
				LOG.w("BODY extra not set or empty");
				return;
			}
			mXMPPService.send(to, body);
			break;
		default:
			LOG.w("Unkown action: " + action);
		}
	}
}
