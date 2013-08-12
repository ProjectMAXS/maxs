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

package org.projectmaxs.shared.module;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * MAXSModuleIntentService is meant for modules to handle their PERFORM_COMMAND
 * intents. This is done in {@link #handleCommand(Command)}, which must be
 * implemented by the modules service.
 * 
 * Extends IntentService, which does a stopSelf() if there are no more remaining
 * intents. Therefore stopSelf() is not needed in this class.
 * 
 * @author Florian Schmaus flo@freakempire.de
 * 
 */
public abstract class MAXSModuleIntentService extends IntentService {
	private final Log mLog;

	public MAXSModuleIntentService(Log log, String name) {
		super(name);
		mLog = log;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initLog(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		mLog.d("onHandleIntent");
		Command command = intent.getParcelableExtra(GlobalConstants.EXTRA_COMMAND);

		Message message = handleCommand(command);
		if (message == null) return;

		// make sure the id is set
		sendMessage(message, command.getId());
	}

	public void sendMessage(Message message, int cmdId) {
		message.setId(cmdId);
		sendMessage(message);
	}

	public void sendMessage(Message message) {
		Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_MESSAGE);
		replyIntent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
		startService(replyIntent);
	}

	public abstract Message handleCommand(Command command);

	public abstract void initLog(Context context);

}
