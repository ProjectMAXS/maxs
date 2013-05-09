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

package org.projectmaxs.main;

import org.jivesoftware.smack.packet.Message;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.shared.Contact;
import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.shared.util.Log;
import org.projectmaxs.shared.xmpp.XMPPMessage;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

public class MAXSLocalService extends Service {

	private XMPPService mXMPPService;

	private final IBinder mBinder = new LocalBinder();

	public void onCreate() {
		Log.initialize("maxs", Settings.getInstance(this).getLogSettings());
		mXMPPService = new XMPPService(this);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			// The service has been killed by Android and we try to restart
			// the connection. This null intent behavior is only for SDK < 9
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				startService(new Intent(Constants.ACTION_START_SERVICE));
			}
			else {
				Log.w("onStartCommand() null intent with Gingerbread or higher");
			}
			return START_STICKY;
		}
		String action = intent.getAction();
		if (action.equals(Constants.ACTION_START_SERVICE)) {
			mXMPPService.connect();
			return START_STICKY;
		}
		else if (action.equals(Constants.ACTION_STOP_SERVICE)) {
			mXMPPService.disconnect();
			return START_NOT_STICKY;
		}
		// TODO everything else
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public MAXSLocalService getService() {
			return MAXSLocalService.this;
		}
	}

	public XMPPService getXMPPService() {
		return mXMPPService;
	}

	public void performCommandFromMessage(Message message) {

	}

	public void startService() {
		Intent intent = new Intent(Constants.ACTION_START_SERVICE);
		mXMPPService.connect();
		startService(intent);
	}

	public void stopService() {
		Intent intent = new Intent(Constants.ACTION_STOP_SERVICE);
		mXMPPService.disconnect();
		startService(intent);
	}

	public void registerModule(ModuleInformation moduleInformation) {

	}

	public Contact getRecentContact() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRecentContact(Contact contact) {
		// TODO Auto-generated method stub

	}

	public Contact getContactFromAlias(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateXMPPStatusInformation(String type, String info) {
		// TODO Auto-generated method stub

	}

	public void sendXMPPMessage(XMPPMessage msg, int id) {
		// TODO Auto-generated method stub

	}
}
