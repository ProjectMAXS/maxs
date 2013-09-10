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

package org.projectmaxs.module.smsnotify;

import java.util.HashMap;
import java.util.Map;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Sms;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Contact;
import org.projectmaxs.shared.mainmodule.ContactUtil;
import org.projectmaxs.shared.module.MAXSBroadcastReceiver;
import org.projectmaxs.shared.module.RecentContactUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSReceiver extends MAXSBroadcastReceiver {
	private static final Log LOG = Log.getLog();

	@Override
	public Message onReceiveReturnMessages(Context context, Intent intent) {
		LOG.d("onReceiveReturnMessages()");
		Map<String, String> msg = RetrieveMessages(intent);
		if (msg == null) {
			LOG.w("Could not retrieve short messages");
			return null;
		}

		Contact contact = null;
		Message message = new Message("New SMS Received");
		for (String sender : msg.keySet()) {
			String smsBody = msg.get(sender);
			LOG.d("Received sms from " + sender + ": " + smsBody);

			contact = ContactUtil.getInstance(context).contactByNumber(sender);
			if (contact == null) contact = new Contact(sender);

			message.add(new Sms(contact.toPrettyString(), smsBody));
		}
		RecentContactUtil.setRecentContact(context, contact);
		return message;
	}

	/**
	 * Compose a map of originating addresses and corresponding short messages
	 * from an android.provider.Telephony.SMS_RECEIVED intent broadcast
	 * 
	 * @param intent
	 * @return a map from originating addresses to the corresponding short
	 *         messages
	 */
	private static Map<String, String> RetrieveMessages(Intent intent) {
		Map<String, String> msg = null;
		SmsMessage[] msgs = null;
		Bundle bundle = intent.getExtras();

		if (bundle == null || !bundle.containsKey("pdus")) return null;

		Object[] pdus = (Object[]) bundle.get("pdus");

		int nbrOfpdus = pdus.length;
		msg = new HashMap<String, String>(nbrOfpdus);
		msgs = new SmsMessage[nbrOfpdus];

		// There can be multiple SMS from multiple senders, there can be
		// a maximum of nbrOfpdus different senders
		// However, send long SMS of same sender in one message
		for (int i = 0; i < nbrOfpdus; i++) {
			msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			String originatinAddress = msgs[i].getOriginatingAddress();

			// Check if index with number exists
			if (!msg.containsKey(originatinAddress)) {
				// Index with number doesn't exist
				// Save string into associative array with sender number
				// as index
				msg.put(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());
			}
			else {
				// Number has been there, add content
				String previousparts = msg.get(originatinAddress);
				String msgString = previousparts + msgs[i].getMessageBody();
				msg.put(originatinAddress, msgString);
			}
		}

		return msg;
	}
}
