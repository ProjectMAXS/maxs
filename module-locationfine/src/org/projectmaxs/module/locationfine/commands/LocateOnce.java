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
import org.projectmaxs.shared.global.util.ServiceTask.IBinderAsInterface;
import org.projectmaxs.shared.global.util.ServiceTask.TimeoutException;
import org.projectmaxs.shared.global.util.SyncServiceTask;
import org.projectmaxs.shared.global.util.SyncServiceTask.PerformSyncTask;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.ILocationFineModuleLocationService;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SharedLocationUtil;

import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;

public class LocateOnce extends AbstractLocate {

	public LocateOnce() {
		super("once", true);
		setHelp(ArgType.NONE, "Try to locate the device once");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws RemoteException, TimeoutException, InterruptedException {
		Intent intent = new Intent(service, LocationService.class);

		SyncServiceTask<ILocationFineModuleLocationService> syncServiceTask = SyncServiceTask
				.builder(service, intent,
						new IBinderAsInterface<ILocationFineModuleLocationService>() {
							@Override
							public ILocationFineModuleLocationService asInterface(IBinder iBinder) {
								return ILocationFineModuleLocationService.Stub.asInterface(iBinder);
							}
						}
						).build();

		startLocationServiceNotSticky(service, command);

		Location location = syncServiceTask
				.performSyncTask(
						new PerformSyncTask<ILocationFineModuleLocationService, RuntimeException, Location>() {
							@Override
							public Location performTask(ILocationFineModuleLocationService service)
									throws RemoteException {
								return service.getCurrentBestLocation();
							}
		});

		Message message;
		if (location != null) {
			message = SharedLocationUtil.toMessage(location);
		} else {
			message = new Message("No location determined yet");
		}

		return message;
	}
}
