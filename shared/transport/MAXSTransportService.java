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

import java.lang.Thread.UncaughtExceptionHandler;

import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.TransportConstants;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public abstract class MAXSTransportService extends Service {
	private static final Log LOG = Log.getLog();

	private static boolean sIsRunning = false;

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
		final String threadName = "MAXSTransportService[" + mName + "]";
		HandlerThread thread = new HandlerThread(threadName);
		thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				LOG.e(threadName + " thread: uncaught Exception!", ex);
			}
		});
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
		msg.what = intent.getAction().hashCode();
		mServiceHandler.sendMessage(msg);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			LOG.d("onStartCommand: null intent received, issuing START_SERVICE");
			intent = new Intent(TransportConstants.ACTION_START_SERVICE);
		}
		LOG.d("onStartCommand: intent=" + intent.getAction() + " flags=" + flags + " startId="
				+ startId + " sIsRunning=" + sIsRunning);

		boolean stickyStart = true;
		final String action = intent.getAction();
		if (TransportConstants.ACTION_STOP_SERVICE.equals(action)) {
			sIsRunning = false;
			stickyStart = false;
		} else if (TransportConstants.ACTION_START_SERVICE.equals(action)) {
			sIsRunning = true;
		}
		// If the service is not running, and we receive something else then
		// START_SERVICE, then don't start sticky to prevent the service from
		// running
		else if (!sIsRunning && !TransportConstants.ACTION_START_SERVICE.equals(action)) {
			LOG.d("onStartCommand: service not running and action (" + action
					+ ") not start. Don't start sticky");
			stickyStart = false;
		}
		performInServiceHandler(intent);
		LOG.d("onStartCommand: stickyStart=" + stickyStart + " action=" + action);
		return stickyStart ? START_STICKY : START_NOT_STICKY;
	}

	public static boolean isRunning() {
		return sIsRunning;
	}

	protected boolean hasMessage(int what) {
		return mServiceHandler.hasMessages(what);
	}

	public abstract void onHandleIntent(Intent intent);
}
