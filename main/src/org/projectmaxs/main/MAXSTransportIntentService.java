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

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.CommandOrigin;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.maintransport.TransportInformation;

import android.content.Intent;

public class MAXSTransportIntentService extends MAXSIntentServiceWithMAXSService {

	private static final Log LOG = Log.getLog();

	private TransportRegistry mTransportRegistry;

	public MAXSTransportIntentService() {
		super(LOG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mTransportRegistry = TransportRegistry.getInstance(this);
	}

	@Override
	protected void onHandleIntent(MAXSService maxsService, Intent intent) {

		String action = intent.getAction();
		LOG.d("handleIntent() Action: " + action);
		if (TransportConstants.ACTION_REGISTER_TRANSPORT.equals(action)) {
			TransportInformation ti = intent
					.getParcelableExtra(TransportConstants.EXTRA_TRANSPORT_INFORMATION);
			mTransportRegistry.registerTransport(ti);
		} else if (GlobalConstants.ACTION_PERFORM_COMMAND.equals(action)) {
			String fullCommand = intent.getStringExtra(TransportConstants.EXTRA_COMMAND);
			CommandOrigin origin = intent
					.getParcelableExtra(TransportConstants.EXTRA_COMMAND_ORIGIN);
			maxsService.performCommand(fullCommand, origin);
		} else if (TransportConstants.ACTION_UPDATE_TRANSPORT_STATUS.equals(action)) {
			String transportPackage = intent.getStringExtra(GlobalConstants.EXTRA_PACKAGE);
			String status = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			mTransportRegistry.updateStatus(transportPackage, status);
		} else {
			throw new IllegalStateException("onHandleIntent: unknown action " + action);
		}
	}
}
