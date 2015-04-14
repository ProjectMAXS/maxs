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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.jul.JULHandler;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

/**
 * MAXSModuleIntentService is meant for modules to handle their PERFORM_COMMAND
 * intents.
 * 
 * @author Florian Schmaus flo@freakempire.de
 * 
 */
public abstract class MAXSModuleIntentService extends Service {
	static {
		JULHandler.setAsDefaultUncaughtExceptionHandler();
	}

	private static final int WHAT = 42;

	private final Log mLog;
	private final String mName;
	private final Map<String, SupraCommand> mCommands;

	private volatile Looper mServiceLooper;
	private volatile ServiceHandler mServiceHandler;
	private volatile Set<Object> mPendingActions = Collections
			.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());

	private String mVersion;

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			onHandleIntent((Intent) msg.obj);
			if (!hasMessages(WHAT) && mPendingActions.isEmpty()) {
				mLog.d("handleMessage: stopSelf hasMessasges=" + hasMessages(WHAT)
						+ " actionsEmpty=" + mPendingActions.isEmpty() + " startId=" + msg.arg1);
				stopSelf(msg.arg1);
			}
		}
	}

	public MAXSModuleIntentService(Log log, String name, SupraCommand[] commands) {
		super();
		mLog = log;
		mName = name;
		mCommands = new HashMap<String, SupraCommand>(commands.length);
		for (SupraCommand command : commands) {
			mCommands.put(command.getCommand(), command);
			// Note that we don't need to insert the short command here
			// If a command is given by the user in the short version, main will already substitute
			// the short for the long version for us
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initLog(this);
		HandlerThread thread = new HandlerThread("MAXSModuleIntentService[" + mName + "]");
		thread.start();

		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		int versionResource = -1;
		try {
			Class<?> r = Class.forName(getPackageName() + ".R$string");
			Field versionField = r.getField("version");
			versionResource = (Integer) versionField.get(null);
		} catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException
				| IllegalArgumentException e) {
			mLog.e("Exception when retrieving version resource ID with reflection", e);
		}

		if (versionResource != -1) {
			mVersion = getString(versionResource);
		} else {
			mVersion = "Unknown";
		}
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
			mLog.d("removePendingAction: stopSelf hasMessasges="
					+ mServiceHandler.hasMessages(WHAT) + " actionsEmpty="
					+ mPendingActions.isEmpty());
			stopSelf();
		}
	}

	protected final void onHandleIntent(Intent intent) {
		mLog.d("onHandleIntent: " + intent.getAction());
		Command command = intent.getParcelableExtra(GlobalConstants.EXTRA_COMMAND);

		org.projectmaxs.shared.global.Message message = null;

		try {
			SupraCommand supraCommand = mCommands.get(command.getCommand());
			if (supraCommand != null) {
				SubCommand subCommand = supraCommand.getSubCommand(command.getSubCommand());
				if (subCommand != null) {
					if (subCommand.requiresArgument() && command.getArgs().isEmpty()) {
						throw new IllegalArgumentException(
								"This command requires an argument but none was given");
					} else {
						message = subCommand.execute(command.getArgs(), command, this);
					}
				} else {
					throw new UnknownSubcommandException(command);
				}
			} else {
				throw new UnknownCommandException(command);
			}
		} catch (Throwable e) {
			mLog.e("onHandleIntent", e);
			Text text = new Text();
			text.addBold("Exception").addNL(" handling command " + command + ": " + e.getMessage());
			text.addItalic("Version: ").addNL(mVersion);
			// Let's also include the stacktrace as String
			text.addWithNewLines(android.util.Log.getStackTraceString(e));
			text.addBoldNL("Further Info");
			text.addItalic("OS Version: ").addNL(
					System.getProperty("os.version") + " (" + Build.VERSION.INCREMENTAL + ")");
			text.addItalic("OS API Level: ").addNL(Integer.toString(Build.VERSION.SDK_INT));
			text.addItalic("Device: ").addNL(Build.DEVICE);
			text.addItalic("Model (and Product): ").addNL(Build.MODEL + " (" + Build.PRODUCT + ")");
			message = new org.projectmaxs.shared.global.Message(text);
		}
		if (message == null) return;

		// make sure the id is set
		send(message, command.getId());
	}

	public abstract void initLog(Context context);

	public final void send(org.projectmaxs.shared.global.Message message, int cmdId) {
		message.setId(cmdId);
		send(message);
	}

	public final void send(org.projectmaxs.shared.global.Message message) {
		MainUtil.send(message, this);
	}
}
