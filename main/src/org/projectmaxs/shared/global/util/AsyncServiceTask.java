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

package org.projectmaxs.shared.global.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;

public abstract class AsyncServiceTask<I extends IInterface> {
	private static final Log LOG = Log.getLog();

	public final Context mContext;

	final Intent mBindIntent;
	final private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = service;
			new PerformTaskThread().start();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// nothing to do here
			LOG.w("Service " + name + " was unexpectedly disconnected");
		}

	};

	private IBinder mService;

	/**
	 * If required, use the application context to avoid
	 * "ServiceConnection leaked" errors
	 * 
	 * @param bindIntent
	 * @param context
	 *            the context used to bind the service.
	 */
	public AsyncServiceTask(Intent bindIntent, Context context) {
		mBindIntent = bindIntent;
		mContext = context;
	}

	protected abstract I asInterface(IBinder iBinder);

	protected abstract void performTask(I iinterface) throws Exception;

	protected void onException(Exception e) {}

	public final boolean go() {
		return mContext.bindService(mBindIntent, mConnection, Context.BIND_AUTO_CREATE);
	}

	private class PerformTaskThread extends Thread {

		@Override
		public void run() {
			I iinterface = asInterface(mService);
			try {
				performTask(iinterface);
			} catch (Exception e) {
				onException(e);
			} finally {
				try {
					mContext.unbindService(mConnection);
				} catch (IllegalArgumentException e) {
					LOG.w("Illegal argument exception while unbinding service. This usually means that the service was not bound while calling unbindService()",
							e);
				}
			}
		}

	}
}
