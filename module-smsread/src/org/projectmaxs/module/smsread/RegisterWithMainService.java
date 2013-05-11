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

package org.projectmaxs.module.smsread;

import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.aidl.IMAXSService;
import org.projectmaxs.shared.util.Log;
import org.projectmaxs.shared.util.Log.LogSettings;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class RegisterWithMainService extends Service {

	private IMAXSService mMAXSService;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		android.os.Debug.waitForDebugger();
		android.util.Log.d("foo", "bar");
		Log.initialize("maxs-smsread", new LogSettings() {

			@Override
			public boolean debugLog() {
				return false;
			}

		});
		Log.d("before bindService()");
		bindService(new Intent(GlobalConstants.ACTION_BIND_MAIN_SERVICE), mConnection, Context.BIND_AUTO_CREATE);
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mMAXSService = IMAXSService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMAXSService = null;
		}

	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("onStartCommand");

		int dc = intent.getIntExtra(GlobalConstants.DELIVER_COUNT, 0);
		if (dc > 3) {
			Log.e("RegisterWithMainService failed because MAXSService could not be bound after 3 attempts");
			return START_NOT_STICKY;
		}

		if (mMAXSService == null) {
			dc++;
			intent.putExtra(GlobalConstants.DELIVER_COUNT, dc);
			startService(intent);
			return START_NOT_STICKY;
		}
		try {
			mMAXSService.registerModule(ModuleService.sMODULE_INFORMATION);
		} catch (RemoteException e) {
			Log.e("Error registering module with main", e);
		}
		unbindService(mConnection);
		stopSelf();
		return START_NOT_STICKY;
	}
}
