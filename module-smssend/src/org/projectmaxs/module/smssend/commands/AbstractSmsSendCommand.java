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

package org.projectmaxs.module.smssend.commands;

import java.util.ArrayList;

import org.projectmaxs.module.smssend.SMSPendingIntentReceiver;
import org.projectmaxs.module.smssend.Settings;
import org.projectmaxs.module.smssend.database.SMSTable;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.messagecontent.Sms;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SmsWriteUtil;
import org.projectmaxs.shared.module.SubCommand;
import org.projectmaxs.shared.module.SupraCommand;

import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;

public abstract class AbstractSmsSendCommand extends SubCommand {

	public static final String PART_NUM_EXTRA = "partNum";
	public static final String CMD_ID_EXTRA = "cmdId";

	public AbstractSmsSendCommand(SupraCommand supraCommand, String name) {
		super(supraCommand, name);
	}

	public AbstractSmsSendCommand(SupraCommand supraCommand, String name,
			boolean isDefaultWithoutArguments, boolean isDefaultWithArguments) {
		super(supraCommand, name, isDefaultWithoutArguments, isDefaultWithArguments);
	}

	MAXSModuleIntentService mService;
	Settings mSettings;

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		if (mService == null) {
			mService = service;
			mSettings = Settings.getInstance(service);
		}
		return null;
	}

	/**
	 * Sends a SMS and tries to add it to the system inbox if smswrite module is
	 * installed
	 * 
	 * @param receiver
	 * @param text
	 * @param cmdId
	 * @param contact
	 * @return
	 */
	final Message sendSms(String receiver, String text, int cmdId, Contact contact) {
		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<PendingIntent> sentIntents = null;
		ArrayList<PendingIntent> deliveryIntents = null;
		ArrayList<String> parts = smsManager.divideMessage(text);
		int partCount = parts.size();
		SMSTable smsTable = SMSTable.getInstance(mService);
		boolean notifySentEnabled = mSettings.notifySentEnabled();
		boolean notifyDeliveredEnabled = mSettings.notifyDeliveredEnabled();

		if (notifySentEnabled || notifyDeliveredEnabled) {
			smsTable.addSms(cmdId, receiver, SharedStringUtil.shorten(text, 20), partCount,
					notifySentEnabled, notifyDeliveredEnabled);
			if (notifySentEnabled) {
				sentIntents = createPendingIntents(partCount, cmdId,
						SMSPendingIntentReceiver.SMS_SENT_ACTION,
						mSettings.getSentIntentRequestCode(partCount));
			}
			if (notifyDeliveredEnabled) {
				deliveryIntents = createPendingIntents(partCount, cmdId,
						SMSPendingIntentReceiver.SMS_DELIVERED_ACTION,
						mSettings.getDeliveredIntentRequestCode(partCount));
			}
		}

		Sms sms = new Sms(receiver, text, Sms.Type.SENT);
		smsManager.sendMultipartTextMessage(receiver, null, parts, sentIntents, deliveryIntents);
		SmsWriteUtil.insertSmsInSystemDB(sms, mService);

		Element sendingSMS = new Element("sms_sending");
		sendingSMS.addChildElement(sms);
		sendingSMS.addChildElement(contact);

		Message message = new Message("Sending SMS to "
				+ SharedStringUtil.prettyPrint(receiver, contact) + ": " + text);
		message.add(sendingSMS);
		return message;
	}

	private final ArrayList<PendingIntent> createPendingIntents(int size, int cmdId, String action,
			int requestCodeStart) {
		ArrayList<PendingIntent> intents = new ArrayList<PendingIntent>(size);
		for (int i = 0; i < size; i++) {
			Intent intent = new Intent(action);
			intent.putExtra(PART_NUM_EXTRA, i);
			intent.putExtra(CMD_ID_EXTRA, cmdId);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(mService,
					requestCodeStart + i, intent, PendingIntent.FLAG_ONE_SHOT);
			intents.add(pendingIntent);
		}
		return intents;
	}
}
