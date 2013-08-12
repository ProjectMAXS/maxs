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

package org.projectmaxs.shared.transport;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public abstract class MAXSTransportService extends Service {
	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;
	private String mName;

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			onHandleIntent((Intent) msg.obj);
		}
	}

	public MAXSTransportService(String name) {
		super();
		mName = name;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		HandlerThread thread = new HandlerThread("MAXSTransportService[" + mName + "]");
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mServiceLooper.quit();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void performInServiceHandler(Intent intent) {
		Message msg = mServiceHandler.obtainMessage();
		msg.obj = intent;
		mServiceHandler.sendMessage(msg);
	}

	public abstract int onStartCommand(Intent intent, int flags, int startId);

	public abstract void onHandleIntent(Intent intent);
}
