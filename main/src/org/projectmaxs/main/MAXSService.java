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
import org.projectmaxs.shared.Contact;
import org.projectmaxs.shared.aidl.IMAXSService;
import org.projectmaxs.shared.xmpp.XMPPMessage;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;

public class MAXSService extends Service {

	private XMPPService mXMPPService;

	@Override
	public IBinder onBind(Intent intent) {
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

	/**
	 * Service used for local binding (i.e. within the .apk)
	 * 
	 */
	public class LocalService extends Service {
		private final IBinder mBinder = new LocalBinder();

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

	}

}
