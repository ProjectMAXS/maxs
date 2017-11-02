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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.projectmaxs.main.database.StatusTable;
import org.projectmaxs.shared.global.StatusInformation;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.CurrentStatus;

import android.content.Context;

public class StatusRegistry extends MAXSService.StartStopListener {

	private static final Log LOG = Log.getLog();

	private static StatusRegistry sStatusRegistry;

	public synchronized static StatusRegistry getInstanceAndInit(Context context) {
		if (sStatusRegistry == null) sStatusRegistry = new StatusRegistry(context);
		return sStatusRegistry;
	}

	private final Map<String, StatusInformation> mStatusInformationMap;

	private final StatusTable mStatusTable;

	private StatusRegistry(Context context) {
		this.mStatusTable = StatusTable.getInstance(context);
		this.mStatusInformationMap = mStatusTable.getAll();

		MAXSService.addStartStopListener(this);
	}

	@Override
	public void onServiceStart(MAXSService service) {
		CurrentStatus status = getCurrentStatus();
		if (status == null) return;
		service.setStatus(status);
	}

	/**
	 * Add the StatusInformation to the registry. If the value has changed or is
	 * not null, return the new status String that should get broadcasted to the
	 * transports.
	 * 
	 * @param infoList
	 * @return the new status
	 */
	public CurrentStatus add(List<StatusInformation> infoList) {
		boolean shouldUpdateStatus = false;
		for (StatusInformation info : infoList) {
			String statusKey = info.getKey();
			String humanValue = info.getHumanValue();
			LOG.d("add: statusKey=" + statusKey + " humanValue=" + humanValue);

			StatusInformation savedStatusValue = mStatusInformationMap.get(statusKey);
			if (savedStatusValue != null) {
				String savedHumanValue = savedStatusValue.getHumanValue();
				if (savedHumanValue != null) {
					if (savedHumanValue.equals(info.getHumanValue())) {
						LOG.d("add: humanValue of '" + statusKey
								+ "' equals savedStatusValue's humaneValuue '" + savedHumanValue
								+ "', not updating");
						continue;
					}
				} else {
					// We found a StatusInformation which has no human readable value, i.e., it's
					// only meant to be exposed in machine readable form. Let us compare its value
					// with the latest saved value.
					if (savedStatusValue.getMachineValue().equals(info.getMachineValue())) {
						LOG.d("add: machineValue of '" + statusKey
								+ "' equals savedStatusValue's machineValuue '"
								+ info.getMachineValue() + "', not updating");
						continue;
					}
				}
			}

			shouldUpdateStatus = true;
			mStatusInformationMap.put(statusKey, info);
			mStatusTable.addStatus(info);
		}
		return shouldUpdateStatus ? getCurrentStatus() : null;
	}

	CurrentStatus getCurrentStatus() {
		if (mStatusInformationMap.isEmpty()) return null;

		List<StatusInformation> statusInformationList = new ArrayList<>(
				mStatusInformationMap.size());

		Iterator<Entry<String, StatusInformation>> it = mStatusInformationMap.entrySet().iterator();

		StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			StatusInformation statusInformation = it.next().getValue();
			statusInformationList.add(statusInformation);
			String humanValue = statusInformation.getHumanValue();
			if (humanValue == null) {
				continue;
			}
			sb.append(statusInformation.getHumanValue());
			if (it.hasNext()) {
				sb.append(" - ");
			}
		}
		return new CurrentStatus(sb.toString(), statusInformationList);
	}
}
