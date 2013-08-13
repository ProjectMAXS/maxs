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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.projectmaxs.main.MAXSService.LocalBinder;
import org.projectmaxs.shared.global.util.Log;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public abstract class MAXSIntentServiceWithMAXSService extends Service {
	private final Queue<Intent> mQueue = new LinkedList<Intent>();
	private final Log mLog;

	private MAXSService mMAXSService;

	public MAXSIntentServiceWithMAXSService(Log log) {
		super();
		mLog = log;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		bindService(new Intent(this, MAXSService.class), mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// not sure if this is really needed
		unbindService(mConnection);
	}

	@Override
	public final int onStartCommand(Intent intent, int flags, int startId) {
		if (mMAXSService == null) {
			mLog.d("onStartCommand: mMAXSService is null, adding to queue");
			mQueue.add(intent);
			// start sticky because there are now intents in the queue to handle
			return START_STICKY;
		}
		else {
			onHandleIntent(mMAXSService, intent);
			stopSelf(startId);
			return START_NOT_STICKY;
		}
	}

	ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mMAXSService = binder.getService();
			if (!mQueue.isEmpty()) {
				mLog.d("onServiceConnected: mQueue not empty, processing");
				Iterator<Intent> it = mQueue.iterator();
				while (it.hasNext())
					onHandleIntent(mMAXSService, it.next());
			}
			stopSelf();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMAXSService = null;
		}
	};

	protected abstract void onHandleIntent(MAXSService maxsService, Intent intent);

	@Override
	public final IBinder onBind(Intent intent) {
		return null;
	}

}
