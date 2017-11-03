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

package org.projectmaxs.module.phonestateread;

import java.util.List;

import org.projectmaxs.shared.module.IPhoneStateReadModuleService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.SmsManager;

public class PhoneStateReadService extends Service {

	private final SmsManager mSmsManager;

	public PhoneStateReadService() {
		super();
		mSmsManager = SmsManager.getDefault();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IPhoneStateReadModuleService.Stub mBinder = new IPhoneStateReadModuleService.Stub() {

		@Override
		public List<String> divideSmsMessage(String message) throws RemoteException {
			return mSmsManager.divideMessage(message);
		}

	};
}