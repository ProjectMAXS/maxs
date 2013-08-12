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

import org.projectmaxs.main.MAXSService.LocalBinder;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.mainmodule.StatusInformation;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MAXSModuleIntentService extends IntentService {

	private static final Log LOG = Log.getLog();

	public MAXSModuleIntentService() {
		super("MAXSService");
	}

	private MAXSService mMAXSLocalService;
	private ModuleRegistry mModuleRegistry;

	@Override
	public void onCreate() {
		super.onCreate();
		Intent intent = new Intent(this, MAXSService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		mModuleRegistry = ModuleRegistry.getInstance(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unbindService(mConnection);
	}

	ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mMAXSLocalService = binder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mMAXSLocalService = null;
		}

	};

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		LOG.d("onHandleIntent: action=" + action);
		if (action.equals(GlobalConstants.ACTION_REGISTER_MODULE)) {
			ModuleInformation mi = intent.getParcelableExtra(GlobalConstants.EXTRA_MODULE_INFORMATION);
			mModuleRegistry.registerModule(mi);
		}
		else if (action.equals(GlobalConstants.ACTION_SEND_MESSAGE)) {
			Message msg = intent.getParcelableExtra(GlobalConstants.EXTRA_MESSAGE);
			mMAXSLocalService.sendMessage(msg);
		}
		else if (action.equals(GlobalConstants.ACTION_SET_RECENT_CONTACT)) {
			String contactNumber = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			mMAXSLocalService.setRecentContact(contactNumber);
		}
		else if (action.equals(GlobalConstants.ACTION_UPDATE_STATUS)) {
			StatusInformation info = intent.getParcelableExtra(GlobalConstants.EXTRA_CONTENT);
			String status = StatusRegistry.getInstanceAndInit(this).add(info);
			if (status != null) mMAXSLocalService.setStatus(status);
		}
		else {
			throw new IllegalStateException("MAXSModuleIntentService unkown action: " + action);
		}
	}
}
