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

import org.projectmaxs.main.database.StatusTable;
import org.projectmaxs.shared.StatusInformation;
import org.projectmaxs.shared.util.Log;

import android.content.Context;

public class StatusRegistry {

	private static final Log LOG = Log.getLog();

	private static StatusRegistry sStatusRegistry;

	public synchronized static StatusRegistry getInstance(Context context) {
		if (sStatusRegistry == null) sStatusRegistry = new StatusRegistry(context);
		return sStatusRegistry;
	}

	private final Context mContext;

	private final Map<String, String> mStatusInformation;

	private final StatusTable mStatusTable;

	private StatusRegistry(Context context) {
		this.mContext = context;
		this.mStatusTable = StatusTable.getInstance(context);

		mStatusInformation = mStatusTable.getAll();
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
		Iterator<String> it = mStatusInformation.values().iterator();
		if (!it.hasNext()) return null;

		StringBuilder sb = new StringBuilder();
		sb.append(it.next());
		while (it.hasNext()) {
			sb.append(" - ");
			sb.append(it.next());
		}
		return sb.toString();
	}
}
