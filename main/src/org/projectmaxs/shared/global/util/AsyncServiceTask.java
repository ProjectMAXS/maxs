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

import java.util.concurrent.Executor;

import android.content.Context;
import android.content.Intent;
import android.os.IInterface;
import android.os.RemoteException;

public class AsyncServiceTask<I extends IInterface, E extends Exception> extends ServiceTask<I> {

	private final PerformAsyncTask<I, E> performAsyncTask;
	private final ExceptionHandler<E> exceptionHandler;
	private final Executor executor;
	private final Class<E> exceptionClass;

	private AsyncServiceTask(AsyncServiceTaskBuilder<I, E> builder) {
		super(builder);
		this.performAsyncTask = builder.performAsyncTask;
		this.exceptionClass = builder.exceptionClass;
		this.exceptionHandler = builder.exceptionHandler;
		this.executor = builder.executor;
	}

	public static <I extends IInterface, E extends Exception> AsyncServiceTaskBuilder<I, E> builder(
			Context context,
			Intent bindIntent,
			IBinderAsInterface<I> iBinderAsInterface,
			PerformAsyncTask<I, E> performAsyncTask,
			Class<E> exceptionClass) {
		return new AsyncServiceTaskBuilder<I, E>(context, bindIntent, iBinderAsInterface,
				performAsyncTask, exceptionClass);
	}

	public interface PerformAsyncTask<I, E extends Exception> {
		void performTask(I service) throws RemoteException, E;
	}

	public interface ExceptionHandler<E extends Exception> {
		void onException(Exception exception, E optionalSpecificException,
				RemoteException optionalRemoteException);
	}

	public void go() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					I serviceInterface = prepareTaskAndPossiblyWaitForService();
					performAsyncTask.performTask(serviceInterface);
				} catch (RemoteException exception) {
					if (exceptionHandler != null) {
						exceptionHandler.onException(exception, null, exception);
						return;
					}
					throw new RuntimeException(exception);
				} catch (Exception exception) {
					if (exceptionHandler != null && exceptionClass.isInstance(exception)) {
						@SuppressWarnings("unchecked")
						E e = (E) exception;
						exceptionHandler.onException(exception, e, null);
						return;
					}
					throw new RuntimeException(exception);
				} finally {
					onTaskFinished();
				}
			}
		};

		if (executor != null) {
			executor.execute(runnable);
		} else {
			Thread thread = new Thread(runnable);
			thread.setDaemon(true);
			thread.start();
		}
	}

	public static class AsyncServiceTaskBuilder<I extends IInterface, E extends Exception>
			extends ServiceTask.Builder<I, AsyncServiceTaskBuilder<I, E>, AsyncServiceTask<I, E>> {

		private final PerformAsyncTask<I, E> performAsyncTask;
		private final Class<E> exceptionClass;

		private ExceptionHandler<E> exceptionHandler = null;
		private Executor executor = null;

		private AsyncServiceTaskBuilder(Context context, Intent bindIntent,
				IBinderAsInterface<I> iBinderAsInterface, PerformAsyncTask<I, E> performAsyncTask,
				Class<E> exceptionClass) {
			super(context, bindIntent, iBinderAsInterface);
			// TODO: Use Objects.requireNonNull once MAXS min API is 19 or higher.
			if (performAsyncTask == null) {
				throw new IllegalArgumentException();
			}
			if (exceptionClass == null) {
				throw new IllegalArgumentException();
			}
			this.performAsyncTask = performAsyncTask;
			this.exceptionClass = exceptionClass;
		}

		@Override
		public AsyncServiceTask<I, E> build() {
			return new AsyncServiceTask<I, E>(this);
		}

		public AsyncServiceTaskBuilder<I, E> withExceptionHandler(
				ExceptionHandler<E> exceptionHandler) {
			this.exceptionHandler = exceptionHandler;
			return getThis();
		}

		public AsyncServiceTaskBuilder<I, E> withExecutor(Executor executor) {
			this.executor = executor;
			return getThis();
		}

		@Override
		protected AsyncServiceTaskBuilder<I, E> getThis() {
			return this;
		}
	};
}
