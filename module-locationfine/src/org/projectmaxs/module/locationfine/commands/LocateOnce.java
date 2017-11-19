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

package org.projectmaxs.module.locationfine.commands;

import org.projectmaxs.module.locationfine.service.LocationService;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.ILocationFineModuleLocationService;
import org.projectmaxs.shared.module.SharedLocationUtil;
import org.projectmaxs.shared.module.MAXSModuleIntentService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;

public class LocateOnce extends AbstractLocate {

	private static final int MAX_WAIT_MILLIS = 200;

	public LocateOnce() {
		super("once", true);
		setHelp(ArgType.NONE, "Try to locate the device once");
	}

	private ILocationFineModuleLocationService mLocationService;

	private final ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mLocationService = ILocationFineModuleLocationService.Stub.asInterface(service);
			synchronized (LocateOnce.this) {
				LocateOnce.this.notify();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mLocationService = null;
		}
	};

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws RemoteException {
		Intent intent = new Intent(service, LocationService.class);
		long locationFineModuleLocationServiceRequestTimestamp = System.currentTimeMillis();
		service.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		startLocationServiceNotSticky(service, command);

		// Wait for mLocationService to become bound.
		synchronized (this) {
			if (mLocationService == null) {
				long remainingWait = MAX_WAIT_MILLIS;
				long waitStart = System.currentTimeMillis();
				do {
					try {
						wait(remainingWait);
					} catch (InterruptedException e) {
						LOG.i("Got interrupted", e);
					}
					if (mLocationService != null) {
						break;
					}
					remainingWait = MAX_WAIT_MILLIS - (System.currentTimeMillis() - waitStart);
				} while (remainingWait > 0);
			}
		}

		if (mLocationService == null) {
			throw new IllegalStateException();
		}

		long bindingTime = System.currentTimeMillis()
				- locationFineModuleLocationServiceRequestTimestamp;
		LOG.d("Took " + bindingTime + "ms to bind the Location service");

		Location location = mLocationService.getCurrentBestLocation();

		Message message;
		if (location != null) {
			message = SharedLocationUtil.toMessage(location);
		} else {
			message = new Message("No location determined yet");
		}
		message.setId(command.getId());

		if (mLocationService != null) {
			service.unbindService(mConnection);
		}

		return message;
	}
}
