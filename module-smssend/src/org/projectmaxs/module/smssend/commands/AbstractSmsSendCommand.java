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
import java.util.List;

import org.projectmaxs.module.smssend.SMSPendingIntentReceiver;
import org.projectmaxs.module.smssend.Settings;
import org.projectmaxs.module.smssend.database.SMSTable;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.messagecontent.Sms;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.PackageManagerUtil;
import org.projectmaxs.shared.global.util.ServiceTask.IBinderAsInterface;
import org.projectmaxs.shared.global.util.ServiceTask.TimeoutException;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.global.util.SyncServiceTask;
import org.projectmaxs.shared.global.util.SyncServiceTask.PerformSyncTask;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.ContactUtil;
import org.projectmaxs.shared.module.IPhoneStateReadModuleService;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.ModuleConstants;
import org.projectmaxs.shared.module.SmsWriteUtil;
import org.projectmaxs.shared.module.SubCommand;
import org.projectmaxs.shared.module.SupraCommand;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.SmsManager;

public abstract class AbstractSmsSendCommand extends SubCommand {

	protected final Log LOG = Log.getLog();

	public static final String PART_NUM_EXTRA = "partNum";
	public static final String CMD_ID_EXTRA = "cmdId";

	public AbstractSmsSendCommand(SupraCommand supraCommand, String name) {
		super(supraCommand, name);
	}

	public AbstractSmsSendCommand(SupraCommand supraCommand, String name,
			boolean isDefaultWithoutArguments, boolean isDefaultWithArguments) {
		super(supraCommand, name, isDefaultWithoutArguments, isDefaultWithArguments);
	}

	private SyncServiceTask<IPhoneStateReadModuleService> mPhoneStateReadModuleServiceTask;


	private PackageManagerUtil mPackageManagerUtil;
	MAXSModuleIntentService mService;
	Settings mSettings;

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		if (mService == null) {
			mService = service;
			mSettings = Settings.getInstance(service);
		}

		if (mPackageManagerUtil == null) {
			mPackageManagerUtil = PackageManagerUtil.getInstance(mService);
		}

		if (mPhoneStateReadModuleServiceTask == null && isModulePhonestateReadInstalled()) {
			Intent intent = new Intent(ModuleConstants.ACTION_BIND_PHONESTATE_READ);
			intent.setClassName(ModuleConstants.PHONESTATE_READ_MODULE_PACKAGE,
					ModuleConstants.PHONSTATE_READ_SERVICE);

			mPhoneStateReadModuleServiceTask = SyncServiceTask.builder(service, intent,
					new IBinderAsInterface<IPhoneStateReadModuleService>() {
						@Override
						public IPhoneStateReadModuleService asInterface(IBinder iBinder) {
							return IPhoneStateReadModuleService.Stub.asInterface(iBinder);
						}
					}).build();
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
	 *            - optional
	 * @return
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws RemoteException
	 */
	final Message sendSms(String receiver, String text, int cmdId, Contact contact)
			throws RemoteException, TimeoutException, InterruptedException {
		SmsManager smsManager = SmsManager.getDefault();
		// Note that sentIntents and deliveryIntents could be null based on the API contract
		// of sendMultipartTextMessage(), which clearly states "if not null". Sadly some devices
		// throw a NPE if those are null, so we initialize them here with an empty ArrayList.
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(0);
		ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(0);

		ArrayList<String> parts;
		try {
			if (text.length() <= 70) {
				// If the text is shorter than 70 chars, then it has less then 70 code points, which
				// is the maximum of a single UCS-2 encoded GSM 03.38 SMS message.
				parts = new ArrayList<>(1);
				parts.add(text);
			} else {
				parts = smsManager.divideMessage(text);
			}
		} catch (SecurityException e) {
			// Some Android devices require the READ_PHONE_STATE permission for
			// divideMessage(String) although the API contract does not mention that this is
			// required. See also https://stackoverflow.com/a/27300529/194894
			parts = maybeDivideMessageViaModulePhonestateRead(text);
		}

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
				+ ContactUtil.prettyPrint(receiver, contact) + ": " + text);
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
					requestCodeStart + i, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
			intents.add(pendingIntent);
		}
		return intents;
	}

	private boolean isModulePhonestateReadInstalled() {
		return mPackageManagerUtil
				.isPackageInstalled(ModuleConstants.PHONESTATE_READ_MODULE_PACKAGE);
	}

	private final ArrayList<String> maybeDivideMessageViaModulePhonestateRead(
			final String message)
			throws RemoteException, TimeoutException, InterruptedException {
		if (!isModulePhonestateReadInstalled()) {
			throw new IllegalStateException(
					"Can not split SMS message as this devices SmsManager.divideMessage() requires the PHONE_STATE_READ permission and MAXS module-phonestateread is not installed. Consider installing module-phonestateread.");
		}

		List<String> res = mPhoneStateReadModuleServiceTask.performSyncTask(
				new PerformSyncTask<IPhoneStateReadModuleService, RuntimeException, List<String>>() {
					@Override
					public List<String> performTask(IPhoneStateReadModuleService service)
							throws RemoteException {
						return service.divideSmsMessage(message);
					}
				});

		return new ArrayList<>(res);
	}
}
