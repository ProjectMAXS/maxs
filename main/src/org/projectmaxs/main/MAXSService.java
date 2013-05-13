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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.packet.Message;
import org.projectmaxs.main.CommandInformation.CommandClashException;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.shared.Command;
import org.projectmaxs.shared.Contact;
import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.shared.util.Log;
import org.projectmaxs.shared.xmpp.XMPPMessage;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

public class MAXSLocalService extends Service {

	private final Map<String, CommandInformation> mCommands = new HashMap<String, CommandInformation>();

	private XMPPService mXMPPService;

	private final IBinder mBinder = new LocalBinder();

	public void onCreate() {
		super.onCreate();
		Log.initialize("maxs", Settings.getInstance(this).getLogSettings());
		mXMPPService = new XMPPService(this);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			// The service has been killed by Android and we try to restart
			// the connection. This null intent behavior is only for SDK < 9
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				startService(new Intent(Constants.ACTION_START_SERVICE));
			}
			else {
				Log.w("onStartCommand() null intent with Gingerbread or higher");
			}
			return START_STICKY;
		}
		String action = intent.getAction();
		if (action.equals(Constants.ACTION_START_SERVICE)) {
			// let's assume that if the size is zero we have to do an initial
			// challenge
			if (mCommands.size() == 0) {
				// clear commands before challenging the modules to register
				mCommands.clear();
				sendBroadcast(new Intent(GlobalConstants.ACTION_REGISTER));
			}
			mXMPPService.connect();
			return START_STICKY;
		}
		else if (action.equals(Constants.ACTION_STOP_SERVICE)) {
			mXMPPService.disconnect();
			return START_NOT_STICKY;
		}
		// TODO everything else
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public MAXSLocalService getService() {
			return MAXSLocalService.this;
		}
	}

	public XMPPService getXMPPService() {
		return mXMPPService;
	}

	public void performCommandFromMessage(Message message) {
		String body = message.getBody();
		if (body == null) return;

		String[] splitedBody = body.split(" ");
		String cmd = splitedBody[0];
		String subCmd = splitedBody[1];
		CommandInformation ci = mCommands.get(cmd);
		if (ci == null) return;

		if (subCmd == null) {
			subCmd = ci.getDefaultSubCommand();
		}
		else if (!ci.isKnownSubCommand(subCmd)) {
			subCmd = ci.getDefaultSubcommandWithArgs();
		}

		if (subCmd == null) return;

		String modulePackage = ci.getPackageForSubCommand(subCmd);
		Intent intent = new Intent(GlobalConstants.ACTION_PERFORM_COMMAND);
		intent.putExtra(GlobalConstants.EXTRA_COMMAND, new Command(cmd, subCmd, null, -1));
		intent.setClassName(modulePackage, modulePackage + "ModuleService");
		startService(intent);
	}

	public void startService() {
		Intent intent = new Intent(Constants.ACTION_START_SERVICE);
		startService(intent);
	}

	public void stopService() {
		Intent intent = new Intent(Constants.ACTION_STOP_SERVICE);
		startService(intent);
	}

	public void registerModule(ModuleInformation moduleInformation) {
		String modulePackage = moduleInformation.getModulePackage();
		Set<ModuleInformation.Command> cmds = moduleInformation.getCommands();
		synchronized (mCommands) {
			for (ModuleInformation.Command c : cmds) {
				String cStr = c.getCommand();
				CommandInformation ci = mCommands.get(cStr);
				if (ci == null) {
					ci = new CommandInformation(cStr);
					mCommands.put(cStr, ci);
				}
				try {
					ci.addSubAndDefCommands(c, modulePackage);
				} catch (CommandClashException e) {
					throw new IllegalStateException(e); // TODO
				}
			}

		}
	}

	public Contact getRecentContact() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRecentContact(Contact contact) {
		// TODO Auto-generated method stub

	}

	public Contact getContactFromAlias(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	public void updateXMPPStatusInformation(String type, String info) {
		// TODO Auto-generated method stub

	}

	public void sendXMPPMessage(XMPPMessage msg, int id) {
		// TODO Auto-generated method stub

	}
}
