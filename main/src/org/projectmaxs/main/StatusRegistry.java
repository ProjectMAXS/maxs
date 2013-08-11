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
import java.util.Map;
import java.util.Map.Entry;

import org.projectmaxs.main.database.StatusTable;
import org.projectmaxs.shared.mainmodule.StatusInformation;

import android.content.Context;

public class StatusRegistry extends MAXSService.StartStopListener {

	private static StatusRegistry sStatusRegistry;

	public synchronized static StatusRegistry getInstanceAndInit(Context context) {
		if (sStatusRegistry == null) sStatusRegistry = new StatusRegistry(context);
		return sStatusRegistry;
	}

	private final Map<String, String> mStatusInformation;

	private final StatusTable mStatusTable;

	private StatusRegistry(Context context) {
		this.mStatusTable = StatusTable.getInstance(context);
		this.mStatusInformation = mStatusTable.getAll();

		MAXSService.addStartStopListener(this);
	}

	@Override
	public void onServiceStart(MAXSService service) {
		String status = updateStatus();
		if (status == null) return;
		service.setStatus(status);
	}

	public String add(StatusInformation info) {
		String statusKey = info.getKey();
		String statusValue = info.getValue();

		String savedStatusValue = mStatusInformation.get(statusKey);
		if (savedStatusValue != null && savedStatusValue.equals(statusValue)) return null;

		mStatusInformation.put(statusKey, statusValue);
		mStatusTable.addStatus(info);
		return updateStatus();
	}

	private String updateStatus() {
		if (mStatusInformation.isEmpty()) return null;

		Iterator<Entry<String, String>> it = mStatusInformation.entrySet().iterator();

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
