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

package org.projectmaxs.module.phonestateread;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.messagecontent.ContactNumber;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.module.ContactUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class PhoneStateService extends Service {

	private static final Log LOG = Log.getLog();

	private TelephonyManager mTelephonyManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LOG.initialize(Settings.getInstance(this));

		boolean stickyStart = true;
		if (intent == null) {
			LOG.d("onStartCommand: null intent, starting service");
			startService();
			return START_STICKY;
		}
		final String action = intent.getAction();
		LOG.d("onStartCommand: " + action);
		if (Constants.START_PHONESTATE_SERVICE.equals(action)) {
			startService();
		} else if (Constants.STOP_PHONESTATE_SERVICE.equals(action)) {
			stopService();
			stopSelf(startId);
			stickyStart = false;
		} else {
			throw new IllegalArgumentException();
		}
		return stickyStart ? START_STICKY : START_NOT_STICKY;
	}

	private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		private boolean mManageIncoming = true;

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				if (!mManageIncoming) {
					mManageIncoming = true;
					send(new Message("Stopped calling"));
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				mManageIncoming = true;
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				if (!mManageIncoming) return;

				mManageIncoming = false;
				String caller = incomingNumber;
				if (incomingNumber == null) {
					// Hidden incomingNumber
					caller = "Hidden Number";
				} else if (ContactNumber.isNumber(incomingNumber)) {
					Contact contact = ContactUtil.getInstance(PhoneStateService.this)
							.contactByNumber(incomingNumber);
					caller = ContactUtil.prettyPrint(contact, incomingNumber);
				}
				send(new Message(caller + " is calling"));
				break;
			}
		}
	};

	private void startService() {
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	private void stopService() {
		if (mTelephonyManager != null) {
			mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
			mTelephonyManager = null;
		}
	}

	public final void send(Message message) {
		Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_MESSAGE);
		replyIntent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
		startService(replyIntent);
	}
}
