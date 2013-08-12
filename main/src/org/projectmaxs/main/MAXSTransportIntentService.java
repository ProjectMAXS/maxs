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
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.CommandOrigin;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.maintransport.TransportInformation;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class MAXSTransportIntentService extends IntentService {

	private static final Log LOG = Log.getLog();

	public MAXSTransportIntentService() {
		super("MAXSService");
	}

	private MAXSService mMAXSLocalService;
	private TransportRegistry mTransportRegistry;

	@Override
	public void onCreate() {
		super.onCreate();
		bindMAXSService();
		mTransportRegistry = TransportRegistry.getInstance(this);
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
		LOG.d("handleIntent() Action: " + action);
		if (TransportConstants.ACTION_REGISTER_TRANSPORT.equals(action)) {
			TransportInformation ti = intent.getParcelableExtra(TransportConstants.EXTRA_TRANSPORT_INFORMATION);
			mTransportRegistry.registerTransport(ti);
		}
		else if (TransportConstants.ACTION_PERFORM_COMMAND.equals(action)) {
			String fullCommand = intent.getStringExtra(TransportConstants.EXTRA_COMMAND);
			CommandOrigin origin = intent.getParcelableExtra(TransportConstants.EXTRA_COMMAND_ORIGIN);

			mMAXSLocalService.performCommand(fullCommand, origin);
		}
		else if (TransportConstants.ACTION_UPDATE_TRANSPORT_STATUS.equals(action)) {
			String transportPackage = intent.getStringExtra(GlobalConstants.EXTRA_PACKAGE);
			String status = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			mTransportRegistry.updateStatus(transportPackage, status);
		}
		else {
			throw new IllegalStateException("onHandleIntent: unkown action " + action);
		}
	}

	private void bindMAXSService() {
		Intent intent = new Intent(this, MAXSService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
}
