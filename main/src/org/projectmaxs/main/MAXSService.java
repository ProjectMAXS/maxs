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

import org.projectmaxs.main.database.CommandTable;
import org.projectmaxs.main.misc.MAXSBatteryManager;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.Contact;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.maintransport.TransportInformation;
import org.projectmaxs.shared.maintransport.TransportInformation.TransportComponent;
import org.projectmaxs.shared.maintransport.TransportOrigin;

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

	public static boolean isRunning() {
		return sIsRunning;
	}

	public static void addStartStopListener(StartStopListener listener) {
		sStartStopListeners.add(listener);
	}

	public static void removeStartStopListener(StartStopListener listener) {
		sStartStopListeners.remove(listener);
	}

	private final Handler mHandler = new Handler();

	// private ConnectivityManager mConnectivityManager;
	private Contact mRecentContact;
	private Runnable mRecentContactRunnable;
	private CommandTable mCommandTable;
	private ModuleRegistry mModuleRegistry;
	private TransportRegistry mTransportRegistry;

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();
		LOG.initialize(Settings.getInstance(this).getLogSettings());
		mCommandTable = CommandTable.getInstance(this);
		mModuleRegistry = ModuleRegistry.getInstance(this);
		mTransportRegistry = TransportRegistry.getInstance(this);

		MAXSBatteryManager.init(this);
		StatusRegistry.getInstanceAndInit(this);

		// Start the service the connection was previously established
		if (Settings.getInstance(this).getServiceState()) {
			LOG.d("onCreate: previous connection state was running, calling startService");
			startService();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			// The service has been killed by Android and we try to restart
			// the connection. This null intent behavior is only for SDK < 9
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				startService(new Intent(Constants.ACTION_START_SERVICE));
			}
			else {
				LOG.w("onStartCommand: null intent with Gingerbread or lower");
			}
			// Returning not sticky here, the start service intent will take
			// care of starting the service sticky
			return START_NOT_STICKY;
		}

		String action = intent.getAction();
		LOG.d("onStartCommand: action=" + action);

		boolean sticky = true;
		if (action.equals(Constants.ACTION_START_SERVICE)) {
			if (sIsRunning) {
				LOG.d("onStartCommand: service already running, nothing to do here");
			}
			else {
				sIsRunning = true;
				sendActionToAllTransportServices(TransportConstants.ACTION_START_SERVICE);
				Settings.getInstance(this).setServiceState(true);
				for (StartStopListener listener : sStartStopListeners)
					listener.onServiceStart(this);
			}
		}
		else if (action.equals(Constants.ACTION_STOP_SERVICE)) {
			sticky = false;
			if (!sIsRunning) {
				LOG.d("onStartCommand: service already stopped, nothing to do here");
			}
			else {
				sendActionToAllTransportServices(TransportConstants.ACTION_STOP_SERVICE);
				Settings.getInstance(this).setServiceState(false);
				for (StartStopListener listener : sStartStopListeners)
					listener.onServiceStop(this);
				stopSelf(startId);
				sIsRunning = false;
			}
		}
		else {
			throw new IllegalStateException("MAXSService unkown action " + action);
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

	public void startService() {
		Intent intent = new Intent(Constants.ACTION_START_SERVICE);
		startService(intent);
	}

	public void stopService() {
		Intent intent = new Intent(Constants.ACTION_STOP_SERVICE);
		startService(intent);
	}

	/**
	 * args can be also in the place of subCmd if the default subCmd is wanted
	 * 
	 * @param command
	 * @param subCmd
	 * @param args
	 * @param origin
	 *            the transport protocol the command arrived with
	 * @param originId
	 *            the id the command arrived with, e.g. in case of XMPP IQ
	 *            commands, the IQ ID
	 * @param issuerInformation
	 *            information to identify the issuer, e.g. in case of XMPP the
	 *            issuers (full) JID
	 */
	public void performCommand(String command, String subCmd, String args, TransportOrigin origin, String originId,
			String issuerInformation) {

		int id = Settings.getInstance(this).getNextCommandId();
		mCommandTable.addCommand(id, command, subCmd, args, origin, issuerInformation, originId);

		CommandInformation ci = mModuleRegistry.get(command);
		if (ci == null) {
			sendMessage(new Message("Unkown command: " + command, id));
			return;
		}

		if (subCmd == null) {
			subCmd = ci.getDefaultSubCommand();
		}
		else if (!ci.isKnownSubCommand(subCmd)) {
			// If subCmd is not known, then maybe it is not really a sub command
			// but instead arguments. Therefore we have to lookup the
			// default sub command when arguments are given, but first assign
			// args to subCmd
			args = subCmd;
			subCmd = ci.getDefaultSubcommandWithArgs();
		}

		if (subCmd == null) {
			sendMessage(new Message("Unknown subCommand: " + subCmd == null ? args : subCmd, id));
			return;
		}

		String modulePackage = ci.getPackageForSubCommand(subCmd);
		Intent intent = new Intent(GlobalConstants.ACTION_PERFORM_COMMAND);
		intent.putExtra(GlobalConstants.EXTRA_COMMAND, new Command(command, subCmd, args, id));
		intent.setClassName(modulePackage, modulePackage + ".ModuleService");
		startService(intent);
	}

	protected Contact getRecentContact() {
		return mRecentContact;
	}

	protected void setRecentContact(final String contactNumber) {
		// TODO lookup number in contacts service
		setRecentContact(new Contact(contactNumber));
	}

	protected synchronized void setRecentContact(final Contact contact) {
		LOG.d("setRecentContact: contact=" + contact);
		if (mRecentContactRunnable != null) {
			mHandler.removeCallbacks(mRecentContactRunnable);
			mRecentContactRunnable = null;
		}
		mRecentContactRunnable = new Runnable() {
			@Override
			public void run() {
				mRecentContact = contact;
				sendMessage(new Message("Recent contact is " + contact));
			}
		};
		mHandler.postDelayed(mRecentContactRunnable, 5000);
	}

	protected Contact getContactFromAlias(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	protected void sendMessage(Message message) {
		final int id = message.getId();
		String originIssuerInfo = null;
		String originId = null;
		TransportOrigin origin = null;
		if (id != Message.NO_ID) {
			CommandTable.Entry entry = mCommandTable.geEntry(id);
			originIssuerInfo = entry.mOriginIssuerInfo;
			origin = entry.mOrigin;
			originId = entry.mOriginId;
		}

		LOG.d("sendMessage() origin='" + origin + "' originIssuerInfo=" + originIssuerInfo + " originId=" + originId
				+ " message=" + message);

		if (origin != null) {
			Intent intent = origin.getIntentFor();
			intent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
			intent.putExtra(TransportConstants.EXTRA_ORIGIN_ISSUER_INFO, originIssuerInfo);
			intent.putExtra(TransportConstants.EXTRA_ORIGIN_ID, originId);
			ComponentName usedTransport = startService(intent);
			if (usedTransport == null) {
				LOG.w("sendMessage: transport not found transportPackage=" + origin.getPackage() + " serviceClass="
						+ origin.getServiceClass());
				// TODO remove origin.getPackage() from module registry
			}
		}
		else {
			// Broadcast this message
			List<TransportInformation> transportList = mTransportRegistry.getAllTransports();
			for (TransportInformation ti : transportList) {
				String transportPackage = ti.getTransportPackage();
				List<TransportComponent> tcList = ti.getAllBroadcastableComponents();
				for (TransportComponent tc : tcList) {
					Intent intent = new Intent(tc.getIntentAction());
					intent.setComponent(new ComponentName(transportPackage, transportPackage
							+ TransportConstants.TRANSPORT_SERVICE));
					intent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
					// no originIssuerInfo or originId info available here
					ComponentName usedTransport = startService(intent);
					if (usedTransport == null) {
						LOG.w("sendMessage: transport not found transportPackage=" + transportPackage
								+ " serviceClass=" + transportPackage + TransportConstants.TRANSPORT_SERVICE);
					}
				}

			}
		}
	}

	protected void setStatus(String status) {
		// TODO set status to all statusable transports
	}

	private void sendActionToAllTransportServices(String action) {
		List<TransportInformation> transports = mTransportRegistry.getAllTransports();
		for (TransportInformation ti : transports) {
			Intent intent = new Intent(action);
			String transportPackage = ti.getTransportPackage();
			intent.setComponent(new ComponentName(transportPackage, transportPackage + ".TransportService"));
			ComponentName cn = startService(intent);
			if (cn == null) {
				LOG.e("sendActionToAllTransportServices: No service found for " + transportPackage);
			}
		}
	}

	public static abstract class StartStopListener {
		public void onServiceStart(MAXSService service) {
		}

		public void onServiceStop(MAXSService service) {
		}
	}
}
