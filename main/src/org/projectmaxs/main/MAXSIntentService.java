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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.projectmaxs.main.MAXSService.LocalBinder;
import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.shared.UserMessage;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MAXSIntentService extends IntentService {

	public MAXSIntentService() {
		super("MAXSService");
	}

	private MAXSService mMAXSLocalService;
	private ModuleRegistry mCommandRegistry;

	private Queue<Intent> mIntentQueue = new LinkedList<Intent>();

	@Override
	public void onCreate() {
		super.onCreate();
		bindMAXSService();
		mCommandRegistry = ModuleRegistry.getInstance(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMAXSLocalService != null) {
			unbindService(mConnection);
			mMAXSLocalService = null;
		}
	}

	ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mMAXSLocalService = binder.getService();
			Iterator<Intent> it = mIntentQueue.iterator();
			while (it.hasNext())
				handleIntent(it.next());
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMAXSLocalService = null;
			// try to rebind the service
			bindMAXSService();
		}

	};

	@Override
	protected void onHandleIntent(Intent intent) {
		if (mMAXSLocalService == null) {
			mIntentQueue.add(intent);
		}
		else {
			handleIntent(intent);
		}
	}

	private void bindMAXSService() {
		Intent intent = new Intent(this, MAXSService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		if (action.equals(GlobalConstants.ACTION_REGISTER_MODULE)) {
			ModuleInformation mi = intent.getParcelableExtra(GlobalConstants.EXTRA_MODULE_INFORMATION);
			mCommandRegistry.registerModule(mi);
		}
		else if (action.equals(GlobalConstants.ACTION_SEND_USER_MESSAGE)) {
			UserMessage msg = intent.getParcelableExtra(GlobalConstants.EXTRA_USER_MESSAGE);
			mMAXSLocalService.sendUserMessage(msg);
		}
		else if (action.equals(GlobalConstants.ACTION_SET_RECENT_CONTACT)) {

		}
		else if (action.equals(GlobalConstants.ACTION_UPDATE_XMPP_STATUS)) {

		}
		else {
			// throw new IllegalStateException();
		}
	}
}
