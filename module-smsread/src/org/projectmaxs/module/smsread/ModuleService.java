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

import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.ModuleInformation;
import org.projectmaxs.shared.ModuleInformation.Command;
import org.projectmaxs.shared.util.Log;
import org.projectmaxs.shared.util.Log.LogSettings;
import org.projectmaxs.shared.xmpp.XMPPMessage;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

public class ModuleService extends IntentService {

	public ModuleService() {
		super("MAXSModule:smsread");
	}

	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation("org.projectmaxs.module.smsread",
			new Command[] { new Command("sms", "read", "read", new String[] { "read" }), });

	@Override
	public void onCreate() {
		super.onCreate();
		Log.initialize("module-smsread", new LogSettings() {
			@Override
			public boolean debugLog() {
				return true;
			}
		});
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("onHandleIntent");
		Command command = (Command) intent.getParcelableExtra(GlobalConstants.EXTRA_COMMAND);

		Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_XMPP_MESSAGE);
		XMPPMessage msg = new XMPPMessage("Hello from smsread module");
		replyIntent.putExtra(GlobalConstants.EXTRA_XMPP_MESSAGE, msg);
		startService(replyIntent);
	}

}
