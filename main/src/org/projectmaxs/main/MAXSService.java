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
import org.projectmaxs.shared.aidl.IMAXSService;
import org.projectmaxs.shared.util.Log;
import org.projectmaxs.shared.xmpp.XMPPMessage;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

public class MAXSService extends Service {

	private XMPPService mXMPPService;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO start local service here?
		return mBinder;
	}

	/**
	 * Used for remote binding (i.e. between different processes/.apk)
	 */
	private final IMAXSService.Stub mBinder = new IMAXSService.Stub() {
		@Override
		public Contact getRecentContact() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setRecentContact(Contact contact) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public Contact getContactFromAlias(String alias) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void updateXMPPStatusInformation(String type, String info) throws RemoteException {
			// TODO Auto-generated method stub

		}

		@Override
		public void sendXMPPMessage(XMPPMessage msg, int id) throws RemoteException {
			// TODO Auto-generated method stub

		}

	};

	private void startService() {
		mXMPPService.connect();
	}

	private void stopService() {
		mXMPPService.disconnect();
		stopSelf();
	}

	/**
	 * Service used for local binding (i.e. within the .apk)
	 * 
	 */
	public class LocalService extends Service {
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
				MAXSService.this.startService();
				return START_STICKY;
			}
			else if (action.equals(Constants.ACTION_STOP_SERVICE)) {
				MAXSService.this.stopService();
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
			LocalService getService() {
				return LocalService.this;
			}
		}

		public XMPPService getXMPPService() {
			return mXMPPService;
		}

		public void performCommandFromMessage(Message message) {

		}

		public void startService() {
			Intent intent = new Intent(Constants.ACTION_START_SERVICE);
			startService(intent);
		}

		public void stopService() {
			Intent intent = new Intent(Constants.ACTION_STOP_SERVICE);
			startService(intent);
		}
	}

}
