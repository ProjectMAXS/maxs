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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.projectmaxs.main.database.CommandTable;
import org.projectmaxs.main.misc.ComposeHelp;
import org.projectmaxs.main.misc.MAXSAmbientTemperatureManager;
import org.projectmaxs.main.misc.MAXSBatteryManager;
import org.projectmaxs.main.misc.StartStopIntentBroadcast;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.RecentContact;
import org.projectmaxs.shared.maintransport.CommandOrigin;
import org.projectmaxs.shared.maintransport.CurrentStatus;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.maintransport.TransportInformation;
import org.projectmaxs.shared.maintransport.TransportInformation.TransportComponent;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

public class MAXSService extends Service {

	private static final Log LOG = Log.getLog();
	private static boolean sIsRunning = false;
	private static final List<StartStopListener> sStartStopListeners = new LinkedList<StartStopListener>();
	private static RecentContact sRecentContact;

	public static boolean isRunning() {
		return sIsRunning;
	}

	public static void addStartStopListener(StartStopListener listener) {
		sStartStopListeners.add(listener);
	}

	public static void removeStartStopListener(StartStopListener listener) {
		sStartStopListeners.remove(listener);
	}

	public static RecentContact getRecentContact() {
		return sRecentContact;
	}

	private final Handler mHandler = new Handler();

	private Runnable mRecentContactRunnable;
	private CommandTable mCommandTable;
	private ModuleRegistry mModuleRegistry;
	private TransportRegistry mTransportRegistry;

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();
		LOG.initialize(Settings.getInstance(this));
		mCommandTable = CommandTable.getInstance(this);
		mModuleRegistry = ModuleRegistry.getInstance(this);
		mTransportRegistry = TransportRegistry.getInstance(this);

		StartStopIntentBroadcast.init();
		MAXSBatteryManager.init(this);
		MAXSAmbientTemperatureManager.init(this);
		PurgeOldCommandsService.init(this);
		StatusRegistry.getInstanceAndInit(this);

		Settings settings = Settings.getInstance(this);
		// Start the service the connection was previously established
		if (settings.getServiceState()) {
			LOG.d("onCreate: previous connection state was running, calling startService");
			startService();
		}
		sRecentContact = settings.getRecentContact();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			// The service has been killed by Android and we try to restart
			// the connection. This null intent behavior is only for SDK < 9
			LOG.w("onStartCommand: null intent with Gingerbread or higher");
			// Returning not sticky here, the start service intent will take
			// care of starting the service sticky
			return START_NOT_STICKY;
		}

		String action = intent.getAction();
		LOG.d("onStartCommand: action=" + action);

		boolean sticky = true;
		switch (action) {
		case Constants.ACTION_START_SERVICE:
			if (sIsRunning) {
				LOG.d("onStartCommand: service already running, nothing to do here");
			} else {
				sIsRunning = true;
				sendActionToAllTransportServices(TransportConstants.ACTION_START_SERVICE);
				Settings.getInstance(this).setServiceState(true);
				for (StartStopListener listener : sStartStopListeners)
					listener.onServiceStart(this);
			}
			break;
		case Constants.ACTION_STOP_SERVICE:
			sticky = false;
			if (!sIsRunning) {
				LOG.d("onStartCommand: service already stopped, nothing to do here");
			} else {
				sendActionToAllTransportServices(TransportConstants.ACTION_STOP_SERVICE);
				Settings.getInstance(this).setServiceState(false);
				for (StartStopListener listener : sStartStopListeners)
					listener.onServiceStop(this);
				stopSelf(startId);
				sIsRunning = false;
			}
			break;
		default:
			throw new IllegalStateException("MAXSService unknown action " + action);
		}
		if (Build.VERSION.SDK_INT >= 31) {
			// Starting apps targeting API level 31 or higher are
			// not allowed to start a sticky foreground service
			// from background.
			sticky = false;
		}
		return sticky ? START_STICKY : START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public MAXSService getService() {
			return MAXSService.this;
		}
	}

	/**
	 * Entry point for commands provided by transports.
	 * 
	 * @param fullCommand
	 * @param origin
	 *            the transport the command arrived with
	 */
	public void performCommand(String fullCommand, CommandOrigin origin) {
		Message errorMsg = null;
		Message helpMsg = null;
		CommandInformation ci = null;
		int id = Settings.getInstance(this).getNextCommandId();
		String[] splitedFullCommand = fullCommand.split(" ", 3);

		String command = splitedFullCommand[0].toLowerCase(Locale.US);
		String subCmd = null;
		if (splitedFullCommand.length > 1) subCmd = splitedFullCommand[1].toLowerCase(Locale.US);
		String args = null;
		if (splitedFullCommand.length > 2) args = splitedFullCommand[2];

		if ("help".equals(command)) {
			helpMsg = ComposeHelp.getHelp(subCmd, args, this);
		} else {
			ci = mModuleRegistry.get(command);
			if (ci == null) {
				errorMsg = new Message("Unknown command: " + command);
			} else {
				// Map a possible short command to the long version, so that the modules only have
				// to care about the long versions
				command = ci.getCommand();

				if (subCmd == null) {
					// User sent just a command without a subcommand, find the default one
					subCmd = ci.getDefaultSubCommand();
					if (subCmd == null) errorMsg = new Message("No default sub command");
				} else if (!ci.isKnownSubCommand(subCmd)) {
					// User sent a String that is not know as subcommand, try to get the default
					// subcommand with arguments
					subCmd = ci.getDefaultSubcommandWithArgs();
					if (subCmd == null) {
						errorMsg = new Message("No default sub command with args");
					} else {
						if (splitedFullCommand.length > 2) {
							args = splitedFullCommand[1] + ' ' + splitedFullCommand[2];
						} else {
							args = splitedFullCommand[1];
						}
					}
				}
			}
		}

		// No matter what happened (normal command, help command or error message), always add the
		// received command to the command table
		mCommandTable.addCommand(id, command, subCmd, args, origin);

		if (errorMsg != null) {
			errorMsg.setId(id);
			send(errorMsg);
		} else if (helpMsg != null) {
			helpMsg.setId(id);
			send(helpMsg);
		} else if (ci != null) {
			String modulePackage = ci.getPackageForSubCommand(subCmd);
			Intent intent = new Intent(GlobalConstants.ACTION_PERFORM_COMMAND);
			intent.putExtra(GlobalConstants.EXTRA_COMMAND, new Command(command, subCmd, args, id));
			intent.setClassName(modulePackage, modulePackage + ".ModuleService");

			ComponentName componentName = startServiceCompat(intent);
			if (componentName == null) {
				// This can happen e.g. due Androids background restriction, if the module was not granted the according permissions. Then we see
				// 11-22 15:20:25.925 W/ActivityManager( 1838): Background start not allowed: service Intent { act=org.projectmaxs.PERFORM_COMMAND
				// cmp=org.projectmaxs.module.contactsread/.ModuleService (has extras)} to org.projectmaxs.module.contactsread /.ModuleService from
				// pid = 1971 uid = 10460 pkg = org.projectmaxs.main startFg ? = false
				// in the log.
				Message errorMessage = new Message("No service was started for " + fullCommand + ". Intent: " + intent);
				send(errorMessage);
			}
		}
	}

	protected synchronized void setRecentContact(final String recentContactInfo,
			final Contact contact) {
		if (sRecentContact != null && sRecentContact.mContactInfo.equals(recentContactInfo)) {
			LOG.d("setRecentContact: Current contact info equals new contact info. Nothing to do.");
			return;
		}
		LOG.d("setRecentContact: recentContactInfo=" + recentContactInfo + " contact=" + contact);
		if (mRecentContactRunnable != null) {
			mHandler.removeCallbacks(mRecentContactRunnable);
			mRecentContactRunnable = null;
		}
		mRecentContactRunnable = new Runnable() {
			@Override
			public void run() {
				sRecentContact = new RecentContact(recentContactInfo, contact);
				Settings.getInstance(MAXSService.this).setRecentContact(sRecentContact);

				Message message = new Message("Recent contact: "
						+ (contact != null ? contact.getDisplayName() + " (" + recentContactInfo
								+ ")" : recentContactInfo));

				if (contact != null) {
					Element recentContactElement = new Element("recent_contact", recentContactInfo);
					recentContactElement.addChildElement(contact);
					message.add(recentContactElement);
				}

				send(message);
			}
		};
		mHandler.postDelayed(mRecentContactRunnable, 5000);
	}

	protected void send(Message message) {
		if (!sIsRunning) {
			LOG.d("send: MAXS main service not running, discarding messsage=" + message);
			return;
		}
		final int id = message.getId();

		CommandOrigin origin = null;
		if (id != Message.NO_ID) {
			CommandTable.Entry entry = mCommandTable.getEntry(id);
			origin = entry.mOrigin;
		}

		LOG.d("send() origin='" + origin + "' message=" + message);

		if (origin != null) {
			Intent intent = origin.getIntentFor();
			intent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
			intent.putExtra(TransportConstants.EXTRA_COMMAND_ORIGIN, origin);
			ComponentName usedTransport = startServiceCompat(intent);
			if (usedTransport == null) {
				LOG.w("send: transport not found transportPackage=" + origin.getPackage()
						+ " serviceClass=" + origin.getServiceClass());
			}
		} else {
			// Broadcast this message
			List<TransportInformation> transportList = mTransportRegistry.getAllTransports();
			for (TransportInformation ti : transportList) {
				String transportPackage = ti.getTransportPackage();
				List<TransportComponent> tcList = ti.getAllBroadcastableComponents();
				for (TransportComponent tc : tcList) {
					Intent intent = new Intent(tc.getIntentAction());
					intent.setClassName(transportPackage, transportPackage
							+ TransportConstants.TRANSPORT_SERVICE);
					intent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
					// no originIssuerInfo or originId info available here
					ComponentName usedTransport = startServiceCompat(intent);
					if (usedTransport == null) {
						LOG.w("send: transport not found transportPackage=" + transportPackage
								+ " serviceClass=" + transportPackage
								+ TransportConstants.TRANSPORT_SERVICE);
					}
				}

			}
		}
	}

	protected void setStatus(CurrentStatus status) {
		LOG.i("Setting status to '" + status.getStatusString() + '\'');
		List<TransportInformation> transportList = mTransportRegistry.getAllTransports();
		for (TransportInformation ti : transportList) {
			if (!ti.supportsStatus()) continue;
			final String transportPackage = ti.getTransportPackage();
			sendCurrentStatus(status, transportPackage);
		}
	}

	void sendCurrentStatus(CurrentStatus status, String transportPackage) {
		final String cls = transportPackage + TransportConstants.TRANSPORT_SERVICE;
		final Intent intent = new Intent(TransportConstants.ACTION_SET_STATUS);
		intent.setClassName(transportPackage, cls);
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, status);
		ComponentName usedTransport = startServiceCompat(intent);
		if (usedTransport == null)
			LOG.w("setStatus: transport not found package=" + transportPackage + " class=" + cls);
	}

	private void sendActionToAllTransportServices(String action) {
		List<TransportInformation> transports = mTransportRegistry.getAllTransports();
		for (TransportInformation ti : transports) {
			Intent intent = new Intent(action);
			String transportPackage = ti.getTransportPackage();
			intent.setClassName(transportPackage, transportPackage
					+ TransportConstants.TRANSPORT_SERVICE);
			ComponentName cn = startServiceCompat(intent);
			if (cn == null) {
				LOG.e("sendActionToAllTransportServices: No service found for " + transportPackage);
			}
		}
	}

	private void startService() {
		Intent intent = new Intent(this, MAXSService.class);
		intent.setAction(Constants.ACTION_START_SERVICE);
		startServiceCompat(intent);
	}

	private ComponentName startServiceCompat(Intent intent) {
		if (Build.VERSION.SDK_INT >= 26) {
			return startForegroundService(intent);
		}
		return startService(intent);
	}

	public static abstract class StartStopListener {
		public void onServiceStart(MAXSService service) {}

		public void onServiceStop(MAXSService service) {}
	}
}
