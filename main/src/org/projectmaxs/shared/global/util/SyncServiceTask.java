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

import android.content.Context;
import android.content.Intent;
import android.os.IInterface;
import android.os.RemoteException;

public class SyncServiceTask<I extends IInterface> extends ServiceTask<I> {

	private SyncServiceTask(SyncServiceTaskBuilder<I> syncServiceTaskBuilder) {
		super(syncServiceTaskBuilder);
	}

	public <E extends Exception, R> R performSyncTask(
			final PerformSyncTask<I, E, R> performSyncTask)
			throws E, TimeoutException, InterruptedException, RemoteException {
		try {
			I serviceInterface = prepareTaskAndPossiblyWaitForService();
			return performSyncTask.performTask(serviceInterface);
		} finally {
			onTaskFinished();
		}
	}

	public static <I extends IInterface> SyncServiceTaskBuilder<I> builder(
			Context context, Intent bindIntent,
			IBinderAsInterface<I> iBinderAsInterface) {
		return new SyncServiceTaskBuilder<I>(context, bindIntent, iBinderAsInterface);
	}

	public interface PerformSyncTask<I, E extends Exception, R> {
		R performTask(I service) throws RemoteException, E;
	}

	public static class SyncServiceTaskBuilder<I extends IInterface>
			extends
			ServiceTask.Builder<I, SyncServiceTaskBuilder<I>, SyncServiceTask<I>> {

		private SyncServiceTaskBuilder(Context context, Intent bindIntent,
				IBinderAsInterface<I> iBinderAsInterface) {
			super(context, bindIntent, iBinderAsInterface);
		}

		@Override
		public SyncServiceTask<I> build() {
			return new SyncServiceTask<I>(this);
		}

		@Override
		protected SyncServiceTaskBuilder<I> getThis() {
			return this;
		}
	};

}
