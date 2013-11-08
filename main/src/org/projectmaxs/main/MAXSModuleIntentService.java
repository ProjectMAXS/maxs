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

import java.util.List;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Contact;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.mainmodule.StatusInformation;

import android.content.Intent;

public class MAXSModuleIntentService extends MAXSIntentServiceWithMAXSService {

	private static final Log LOG = Log.getLog();

	private ModuleRegistry mModuleRegistry;

	public MAXSModuleIntentService() {
		super(LOG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mModuleRegistry = ModuleRegistry.getInstance(this);
	}

	@Override
	protected void onHandleIntent(MAXSService maxsService, Intent intent) {
		if (intent == null) {
			LOG.i("onHandleIntent: null intent");
			return;
		}

		String action = intent.getAction();
		LOG.d("onHandleIntent: action=" + action);
		if (action.equals(GlobalConstants.ACTION_REGISTER_MODULE)) {
			ModuleInformation mi = intent
					.getParcelableExtra(GlobalConstants.EXTRA_MODULE_INFORMATION);
			mModuleRegistry.registerModule(mi);
		} else if (action.equals(GlobalConstants.ACTION_SEND_MESSAGE)) {
			Message msg = intent.getParcelableExtra(GlobalConstants.EXTRA_MESSAGE);
			maxsService.send(msg);
		} else if (action.equals(GlobalConstants.ACTION_SET_RECENT_CONTACT)) {
			String usedContactInfo = intent.getStringExtra(GlobalConstants.EXTRA_CONTENT);
			Contact contact = intent.getParcelableExtra(GlobalConstants.EXTRA_CONTACT);
			maxsService.setRecentContact(usedContactInfo, contact);
		} else if (action.equals(GlobalConstants.ACTION_UPDATE_STATUS)) {
			List<StatusInformation> infoList = intent
					.getParcelableArrayListExtra(GlobalConstants.EXTRA_CONTENT);
			String status = StatusRegistry.getInstanceAndInit(this).add(infoList);
			// only set the status if something has changed
			if (status != null) maxsService.setStatus(status);
		} else {
			throw new IllegalStateException("MAXSModuleIntentService unknown action: " + action);
		}
	}
}
