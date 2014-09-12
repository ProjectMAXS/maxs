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

package org.projectmaxs.shared.global.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;

public class PackageManagerUtil {

	private static PackageManagerUtil sPackageManagerUtil;

	public synchronized static PackageManagerUtil getInstance(Context context) {
		if (sPackageManagerUtil == null) sPackageManagerUtil = new PackageManagerUtil(context);
		return sPackageManagerUtil;
	}

	private final PackageManager mPackageManager;
	private final String mPackageName;

	private PackageManagerUtil(Context context) {
		mPackageManager = context.getPackageManager();
		mPackageName = context.getPackageName();
	}

	public boolean isPackageInstalled(String packageName) {
		for (PackageInfo pi : mPackageManager.getInstalledPackages(0)) {
			if (packageName.equals(pi.packageName)) return true;
		}
		return false;
	}

	public boolean isIntentAvailable(Intent intent) {
		List<ResolveInfo> list = mPackageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	public boolean isSystemApp() {
		ApplicationInfo applicationInfo;
		try {
			applicationInfo = mPackageManager.getApplicationInfo(mPackageName, 0);
		} catch (NameNotFoundException e) {
			throw new IllegalStateException(e);
		}
		return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
	}
}
