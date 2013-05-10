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

package org.projectmaxs.module.smsread;

import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.shared.ModuleInformation.Command;
import org.projectmaxs.shared.aidl.IMAXSModuleService;
import org.projectmaxs.shared.xmpp.XMPPMessage;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class ModuleService extends Service {

	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation("org.projectmaxs.module.smsread",
			new Command[] { new Command("sms", "read", "read", new String[] { "read" }), });

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private final IMAXSModuleService.Stub mBinder = new IMAXSModuleService.Stub() {

		@Override
		public XMPPMessage executeCommand(String cmd, String subCmd, String args, int cmdID) throws RemoteException {
			XMPPMessage msg = new XMPPMessage();
			msg.add("reply from smsread module");
			return msg;
		}

	};

}
