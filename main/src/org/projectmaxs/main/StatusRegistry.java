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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.projectmaxs.main.database.StatusTable;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.StatusInformation;

import android.content.Context;

public class StatusRegistry extends MAXSService.StartStopListener {

	private static final Log LOG = Log.getLog();

	private static StatusRegistry sStatusRegistry;

	public synchronized static StatusRegistry getInstanceAndInit(Context context) {
		if (sStatusRegistry == null) sStatusRegistry = new StatusRegistry(context);
		return sStatusRegistry;
	}

	private final Map<String, String> mStatusInformationMap;

	private final StatusTable mStatusTable;

	private StatusRegistry(Context context) {
		this.mStatusTable = StatusTable.getInstance(context);
		this.mStatusInformationMap = mStatusTable.getAll();

		MAXSService.addStartStopListener(this);
	}

	@Override
	public void onServiceStart(MAXSService service) {
		String status = updateStatus();
		if (status == null) return;
		service.setStatus(status);
	}

	/**
	 * Add the StatusInformation to the registry. If the value has changed or is
	 * not null, return the new status String that should get broadcasted to the
	 * transports.
	 * 
	 * @param info
	 * @return
	 */
	public String add(List<StatusInformation> infoList) {
		boolean shouldUpdateStatus = false;
		for (StatusInformation info : infoList) {
			String statusKey = info.getKey();
			String statusValue = info.getValue();
			LOG.d("add: statusKey=" + statusKey + " statusValue=" + statusValue);

			String savedStatusValue = mStatusInformationMap.get(statusKey);
			if (savedStatusValue != null && savedStatusValue.equals(statusValue)) {
				LOG.d("add: statusValue equals savedStatusValue, not updating");
				continue;
			} else {
				shouldUpdateStatus = true;
			}

			mStatusInformationMap.put(statusKey, statusValue);
			mStatusTable.addStatus(info);
		}
		return shouldUpdateStatus ? updateStatus() : null;
	}

	private String updateStatus() {
		if (mStatusInformationMap.isEmpty()) return null;

		Iterator<Entry<String, String>> it = mStatusInformationMap.entrySet().iterator();

		StringBuilder sb = new StringBuilder();
		String value = it.next().getValue();
		sb.append(value);
		while (it.hasNext()) {
			sb.append(" - ");
			value = it.next().getValue();
			sb.append(value);
		}
		return sb.toString();
	}
}
