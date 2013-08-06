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
import org.projectmaxs.main.xmpp.XMPPService;
import org.projectmaxs.shared.Command;
import org.projectmaxs.shared.Contact;
import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.Message;
import org.projectmaxs.shared.util.Log;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

	private final Handler mHandler = new Handler();
	private ConnectivityManager mConnectivityManager;

	private XMPPService mXMPPService;

	private Contact mRecentContact;
	private Runnable mRecentContactRunnable;
	private CommandTable mCommandTable;
	private ModuleRegistry mCommandRegistry;

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();
		LOG.initialize(Settings.getInstance(this).getLogSettings());
		mXMPPService = new XMPPService(this);
		mCommandTable = CommandTable.getInstance(this);
		mCommandRegistry = ModuleRegistry.getInstance(this);
		mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		MAXSBatteryManager.init(this);
		StatusRegistry.getInstanceAndInit(this);

		// Start the service the connection was previously established
		if (Settings.getInstance(this).getServiceState()) {
			LOG.d("onCreate: previous connectin state was running, calling startService");
			startService();
		}
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			// The service has been killed by Android and we try to restart
			// the connection. This null intent behavior is only for SDK < 9
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				startService(new Intent(Constants.ACTION_START_SERVICE));
			}
			else {
				LOG.w("onStartCommand() null intent with Gingerbread or higher");
			}
			return START_STICKY;
		}
		String action = intent.getAction();
		LOG.d("onStartCommand(): action=" + action);

		if (action.equals(Constants.ACTION_START_SERVICE)) {
			sIsRunning = true;
			mXMPPService.connect();
			Settings.getInstance(this).setServiceState(true);
			for (StartStopListener listener : sStartStopListeners)
				listener.onServiceStart(this);
			return START_STICKY;
		}
		else if (action.equals(Constants.ACTION_STOP_SERVICE)) {
			mXMPPService.disconnect();
			Settings.getInstance(this).setServiceState(false);
			for (StartStopListener listener : sStartStopListeners)
				listener.onServiceStop(this);
			stopSelf(startId);
			sIsRunning = false;
			return START_NOT_STICKY;
		}
		else if (action.equals(Constants.ACTION_NETWORK_STATUS_CHANGED)) {
			boolean connected = intent.getBooleanExtra(Constants.EXTRA_NETWORK_CONNECTED, false);
			boolean networkTypeChanged = intent.getBooleanExtra(Constants.EXTRA_NETWORK_TYPE_CHANGED, false);
			mXMPPService.newConnecitivytInformation(connected, networkTypeChanged);
		}
		// TODO everything else
		return START_STICKY;
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

	public XMPPService getXMPPService() {
		return mXMPPService;
	}

	public enum CommandOrigin {
		// @formatter:off
		UNKOWN, // If message is not created as response of a command
		XMPP_MESSAGE,
		XMPP_IQ
		// @formatter:on
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
	public void performCommand(String command, String subCmd, String args, CommandOrigin origin, String originId,
			String issuerInformation) {

		int id = Settings.getInstance(this).getNextCommandId();
		mCommandTable.addCommand(id, command, subCmd, args, origin, issuerInformation, originId);

		CommandInformation ci = mCommandRegistry.get(command);
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

	public boolean dataConnectionAvailable() {
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		if (activeNetwork == null) return false;
		return activeNetwork.isConnected();
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
		CommandOrigin commandOrigin = CommandOrigin.UNKOWN;
		if (id != Message.NO_ID) {
			CommandTable.Entry entry = mCommandTable.geEntry(id);
			originIssuerInfo = entry.mOriginIssuerInfo;
			commandOrigin = entry.mOrigin;
			originId = entry.mOriginId;
		}

		LOG.d("sendMessage() commandOrigin=" + commandOrigin + " originIssuerInfo=" + originIssuerInfo + " originId="
				+ originId + " message=" + message);
		switch (commandOrigin) {
		case XMPP_MESSAGE:
			mXMPPService.sendAsMessage(message, originIssuerInfo, originId);
			break;
		case XMPP_IQ:
			mXMPPService.sendAsIQ(message, originIssuerInfo, originId);
			break;
		default:
			// if we could not determine the command origin, broadcast to all
			mXMPPService.sendAsMessage(message, null, null);
			break;
		}

	}

	protected void setStatus(String status) {
		mXMPPService.setStatus(status);
	}

	public static abstract class StartStopListener {
		public void onServiceStart(MAXSService service) {
		}

		public void onServiceStop(MAXSService service) {
		}
	}
}
