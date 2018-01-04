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

public abstract class ServiceTask<I extends IInterface> {

	private static final long DEFAULT_WAIT_MILLIS = 1500;

	protected static final Log LOG = Log.getLog();

	private final Context context;
	private final IBinderAsInterface<I> iBinderAsInterface;
	private final Intent bindIntent;
	private final long maxWaitMillis;
	private final int bindFlags;

	private long bindRequestIssuedTimestamp;

	private int runningTasks;

	private IBinder service;

	private final ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			long bindingTime = System.currentTimeMillis() - bindRequestIssuedTimestamp;
			LOG.d("Service " + name + " bound after " + bindingTime + "ms");

			ServiceTask.this.service = service;

			synchronized (ServiceTask.this) {
				ServiceTask.this.notifyAll();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			synchronized (ServiceTask.this) {
				ServiceTask.this.service = null;
				ServiceTask.this.notifyAll();
			}
			LOG.w("Service " + name + " was unexpectedly disconnected");
		}

	};

	@Override
	public String toString() {
		return getClass().getSimpleName() + ' ' + bindIntent;
	}

	/**
	 * If required, use the application context to avoid
	 * "ServiceConnection leaked" errors
	 *
	 * @param <B>
	 *
	 * @param bindIntent
	 * @param context
	 *            the context used to bind the service.
	 */
	protected <B extends Builder<I, B, T>, T extends ServiceTask<I>> ServiceTask(
			Builder<I, B, T> builder) {
		this.bindIntent = builder.bindIntent;
		this.context = builder.context;
		this.iBinderAsInterface = builder.iBinderAsInterface;
		this.maxWaitMillis = builder.maxWaitMillis;
		this.bindFlags = builder.bindFlags;
	}

	protected final I prepareTaskAndPossiblyWaitForService()
			throws InterruptedException, TimeoutException {
		// Wait for mLocationService to become bound.
		synchronized (this) {
			if (runningTasks == 0) {
				// Bind the service.
				bindRequestIssuedTimestamp = System.currentTimeMillis();
				boolean bound = context.bindService(bindIntent, serviceConnection, bindFlags);
				if (!bound) {
					throw new IllegalArgumentException();
				}
			} else if (runningTasks == Integer.MAX_VALUE) {
				throw new IllegalStateException();
			}

			runningTasks++;

			if (service == null) {
				long remainingWait = maxWaitMillis;
				long waitStart = System.currentTimeMillis();
				do {
					wait(remainingWait);
					if (service != null) {
						break;
					}
					remainingWait = maxWaitMillis - (System.currentTimeMillis() - waitStart);
				} while (remainingWait > 0);
			}
		}

		if (service == null) {
			throw new TimeoutException(this);
		}

		return iBinderAsInterface.asInterface(service);
	}

	protected final synchronized void onTaskFinished() {
		runningTasks--;
		if (runningTasks > 0) {
			LOG.d("Not going to unbind service  because there are " + runningTasks
					+ " runnings tasks left for " + bindIntent);
			return;
		}
		final IBinder service = this.service;
		if (service == null) {
			LOG.d("No running tasks left and service already unboud for " + bindIntent);
			return;
		}
		try {
			LOG.d("Unbinding service for " + bindIntent);
			context.unbindService(serviceConnection);
		} catch (IllegalStateException e) {
			LOG.w("IllegalStateException while unbinding. Service was not bound?", e);
		} finally {
			this.service = null;
		}
	}

	public interface IBinderAsInterface<I> {
		I asInterface(IBinder iBinder);
	}

	public static class TimeoutException extends Exception {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		private TimeoutException(ServiceTask<?> serviceTask) {
			super("The service was not bound after " + serviceTask.maxWaitMillis + "ms");
		}
	}

	public static abstract class Builder<I extends IInterface, B extends Builder<I, B, T>, T extends ServiceTask<I>> {

		private final Context context;
		private final Intent bindIntent;
		private final IBinderAsInterface<I> iBinderAsInterface;

		private int bindFlags = Context.BIND_AUTO_CREATE;
		private long maxWaitMillis = DEFAULT_WAIT_MILLIS;

		protected Builder(Context context, Intent bindIntent,
				IBinderAsInterface<I> iBinderAsInterface) {
			// TODO: Use Objects.requireNonNull once MAXS min API is 19 or higher.
			if (context == null) {
				throw new IllegalArgumentException();
			}
			if (bindIntent == null) {
				throw new IllegalArgumentException();
			}
			if (iBinderAsInterface == null) {
				throw new IllegalArgumentException();
			}
			this.context = context;
			this.bindIntent = bindIntent;
			this.iBinderAsInterface = iBinderAsInterface;
		}

		public B setMaxWaitMillis(long maxWaitMillis) {
			this.maxWaitMillis = maxWaitMillis;
			return getThis();
		}

		public B setBindFlags(int bindFlags) {
			this.bindFlags = bindFlags;
			return getThis();
		}

		public abstract T build();

		protected abstract B getThis();
	}
}
