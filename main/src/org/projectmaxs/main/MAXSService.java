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

import org.projectmaxs.main.MAXSLocalService.LocalBinder;
import org.projectmaxs.shared.Contact;
import org.projectmaxs.shared.aidl.IMAXSService;
import org.projectmaxs.shared.xmpp.XMPPMessage;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class MAXSService extends Service {
	private MAXSLocalService mMAXSLocalService;

	@Override
	public IBinder onBind(Intent i) {
		if (mMAXSLocalService == null) {
			Intent intent = new Intent(this, MAXSLocalService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (mMAXSLocalService != null) {
			unbindService(mConnection);
			mMAXSLocalService = null;
		}
		return false;
	}

	ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mMAXSLocalService = binder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMAXSLocalService = null;
		}

	};

	private final IMAXSService.Stub mBinder = new IMAXSService.Stub() {
		@Override
		public Contact getRecentContact() throws RemoteException {
			return mMAXSLocalService.getRecentContact();
		}

		@Override
		public void setRecentContact(Contact contact) throws RemoteException {
			mMAXSLocalService.setRecentContact(contact);
		}

		@Override
		public Contact getContactFromAlias(String alias) throws RemoteException {
			return mMAXSLocalService.getContactFromAlias(alias);
		}

		@Override
		public void updateXMPPStatusInformation(String type, String info) throws RemoteException {
			mMAXSLocalService.updateXMPPStatusInformation(type, info);
		}

		@Override
		public void sendXMPPMessage(XMPPMessage msg, int id) throws RemoteException {
			mMAXSLocalService.sendXMPPMessage(msg, id);
		}

	};

}
