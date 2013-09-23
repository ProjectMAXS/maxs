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

package org.projectmaxs.module.smssend;

import org.projectmaxs.module.smssend.database.SMSTable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

public class SMSPendingIntentReceiver extends BroadcastReceiver {

	public static final String SMS_SENT_ACTION = ModuleService.PACKAGE + "SMS_SENT";
	public static final String SMS_DELIVERED_ACTION = ModuleService.PACKAGE + "SMS_DELIVERED";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		int partNum = intent.getIntExtra(ModuleService.PART_NUM_EXTRA, -1);
		int cmdId = intent.getIntExtra(ModuleService.CMD_ID_EXTRA, -1);
		int res = getResultCode();
		SMSTable smsTable = SMSTable.getInstance(context);
		if (SMS_SENT_ACTION.equals(action)) {
			String sentIntents = smsTable.getIntents(cmdId, SMSTable.IntentType.SENT);
			sentIntents = markPart(sentIntents, partNum, smsResultToChar(res));
			smsTable.updateIntents(cmdId, sentIntents, SMSTable.IntentType.SENT);
			if (allMarkedNoError(sentIntents)) {
				// TODO send 'sms sent' message
			}
			// TODO Add mechanism to display sent failure reasons
		} else if (SMS_DELIVERED_ACTION.equals(action)) {
			String deliveredIntents = smsTable.getIntents(cmdId, SMSTable.IntentType.DELIVERED);
			deliveredIntents = markPart(deliveredIntents, partNum, RESULT_NO_ERROR_CHAR);
			smsTable.updateIntents(cmdId, deliveredIntents, SMSTable.IntentType.DELIVERED);
			if (allMarkedNoError(deliveredIntents)) {
				// TODO send 'sms delivered' message
			}
		} else {
			throw new IllegalStateException("Unkown action=" + action
					+ " in SMSPendingIntentReceiver");
		}
	}

	private static String markPart(String intents, int partNum, char mark) {
		char[] intentsChars = intents.toCharArray();
		intentsChars[partNum] = mark;
		return new String(intentsChars);
	}

	private static boolean allMarkedNoError(String intents) {
		char[] intentsChars = intents.toCharArray();
		for (int i = 0; i < intentsChars.length; i++)
			if (intentsChars[i] != RESULT_NO_ERROR_CHAR) return false;

		return true;
	}

	private static final char RESULT_NO_ERROR_CHAR = 'X';
	private static final char RESULT_ERROR_GENERIC_FAILURE_CHAR = 'G';
	private static final char RESULT_ERROR_NO_SERVICE_CHAR = 'S';
	private static final char RESULT_ERROR_NULL_PDU_CHAR = 'P';
	private static final char RESULT_ERROR_RADIO_OFF_CHAR = 'R';

	private static char smsResultToChar(int res) {
		switch (res) {
		case 0:
			return RESULT_NO_ERROR_CHAR;
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			return RESULT_ERROR_GENERIC_FAILURE_CHAR;
		case SmsManager.RESULT_ERROR_NO_SERVICE:
			return RESULT_ERROR_NO_SERVICE_CHAR;
		case SmsManager.RESULT_ERROR_NULL_PDU:
			return RESULT_ERROR_NULL_PDU_CHAR;
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			return RESULT_ERROR_RADIO_OFF_CHAR;
		default:
			throw new IllegalStateException("unkown res=" + res);
		}
	}
}
