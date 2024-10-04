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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

public abstract class MAXSIntentServiceWithMAXSService extends Service {
	private static final String NOTIFICATION_CHANNEL_ID = "maxs";

	private final Queue<Intent> mQueue = new LinkedList<Intent>();
	private final String mName;
	private final Log mLog;

	private MAXSService mMAXSService;

	public MAXSIntentServiceWithMAXSService(String name, Log log) {
		super();
		mName = name;
		mLog = log;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		bindService(new Intent(this, MAXSService.class), mConnection, Context.BIND_AUTO_CREATE);

		if (Build.VERSION.SDK_INT >= 26) {
			String name = "MAXS";
			String description = "MAXS Intent Service";
			int importance = NotificationManager.IMPORTANCE_MIN;

			NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
			channel.setDescription(description);

			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
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

			if (Build.VERSION.SDK_INT >= 26) {
				Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
								.setContentText(mName)
								.build();
				int notificationId = 1;

				if (Build.VERSION.SDK_INT >= 34) {
					startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
				} else {
					startForeground(notificationId, notification);
				}
			}

			// start stikcy because there are now intents in the queue to handle
			int res = START_STICKY;
			if (Build.VERSION.SDK_INT >= 31) {
				// Starting apps targeting API level 31 or higher are
				// not allowed to start a sticky foreground service
				// from background.
				res = START_NOT_STICKY;
			}
			return res;
		} else {
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
