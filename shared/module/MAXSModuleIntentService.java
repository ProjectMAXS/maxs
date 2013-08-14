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

package org.projectmaxs.shared.module;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

/**
 * MAXSModuleIntentService is meant for modules to handle their PERFORM_COMMAND
 * intents. This is done in {@link #handleCommand(Command)}, which must be
 * implemented by the modules service.
 * 
 * 
 * @author Florian Schmaus flo@freakempire.de
 * 
 */
public abstract class MAXSModuleIntentService extends Service {
	private static final int WHAT = 42;

	private final Log mLog;
	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;
	private volatile Set<Object> mPendingActions = Collections.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());
	private String mName;

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			onHandleIntent((Intent) msg.obj);
			if (!hasMessages(WHAT) && mPendingActions.isEmpty()) {
				mLog.d("handleMessage: stopSelf hasMessasges=" + hasMessages(WHAT) + " actionsEmpty="
						+ mPendingActions.isEmpty() + " startId=" + msg.arg1);
				stopSelf(msg.arg1);
			}
		}
	}

	public MAXSModuleIntentService(Log log, String name) {
		super();
		mLog = log;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initLog(this);
		HandlerThread thread = new HandlerThread("MAXSModuleIntentService[" + mName + "]");
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}

	@Override
	public void onDestroy() {
		mServiceLooper.quit();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Message msg = mServiceHandler.obtainMessage();
		msg.arg1 = startId;
		msg.obj = intent;
		msg.what = 42;
		mServiceHandler.sendMessage(msg);
		return START_NOT_STICKY;
	}

	public final void addPendingAction(Object action) {
		mPendingActions.add(action);
	}

	public final void removePendingAction(Object action) {
		mPendingActions.remove(action);
		if (!mServiceHandler.hasMessages(WHAT) && mPendingActions.isEmpty()) {
			mLog.d("removePendingAction: stopSelf hasMessasges=" + mServiceHandler.hasMessages(WHAT) + " actionsEmpty="
					+ mPendingActions.isEmpty());
			stopSelf();
		}
	}

	protected final void onHandleIntent(Intent intent) {
		mLog.d("onHandleIntent: " + intent.getAction());
		Command command = intent.getParcelableExtra(GlobalConstants.EXTRA_COMMAND);

		org.projectmaxs.shared.global.Message message;

		if ("help".equals(command.getCommand())) {
			message = getHelp(command.getSubCommand(), command.getArgs());
		}
		else {
			message = handleCommand(command);
			if (message == null) return;
		}

		// make sure the id is set
		sendMessage(message, command.getId());
	}

	public abstract org.projectmaxs.shared.global.Message handleCommand(Command command);

	public abstract void initLog(Context context);

	/**
	 * Modules need to override this method to provide help for their commands
	 * 
	 * @param command
	 * @param subCommand
	 * @return
	 */
	public org.projectmaxs.shared.global.Message getHelp(String command, String subCommand) {
		return new org.projectmaxs.shared.global.Message("Help for '" + command + " " + subCommand + "' not available");
	}

	public final void sendMessage(org.projectmaxs.shared.global.Message message, int cmdId) {
		message.setId(cmdId);
		sendMessage(message);
	}

	public final void sendMessage(org.projectmaxs.shared.global.Message message) {
		Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_MESSAGE);
		replyIntent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
		startService(replyIntent);
	}

}
