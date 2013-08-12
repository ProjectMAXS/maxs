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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageManagerUtil {

	private static PackageManagerUtil sPackageManagerUtil;

	public synchronized static PackageManagerUtil getInstance(Context context) {
		if (sPackageManagerUtil == null) sPackageManagerUtil = new PackageManagerUtil(context);
		return sPackageManagerUtil;
	}

	final PackageManager mPackageManager;

	private PackageManagerUtil(Context context) {
		mPackageManager = context.getPackageManager();
	}

	public boolean isPackageInstalled(String packageName) {
		for (PackageInfo pi : mPackageManager.getInstalledPackages(0)) {
			if (packageName.equals(pi.packageName)) return true;
		}
		return false;
	}
}
