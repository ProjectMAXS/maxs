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
import org.projectmaxs.shared.global.messagecontent.Sms;
import org.projectmaxs.shared.global.util.PackageManagerUtil;
import org.projectmaxs.shared.mainmodule.MainModuleConstants;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class SmsWriteUtil {

	public static final String SMS_WRITE_MODULE_PACKAGE = GlobalConstants.MODULE_PACKAGE
			+ ".smswrite";

	public static boolean insertSmsInSystemDB(Sms sms, Context context) {
		if (!PackageManagerUtil.getInstance(context).isPackageInstalled(SMS_WRITE_MODULE_PACKAGE))
			return false;

		Intent intent = new Intent(MainModuleConstants.ACTION_SMS_TO_INBOX);
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, sms);
		intent.setClassName(ModuleConstants.SMSWRITE_MODULE_PACKAGE,
				ModuleConstants.SMSWRITE_SERVICE);
		ComponentName componentName = context.startService(intent);
		if (componentName == null) throw new IllegalStateException("Component not found");
		return true;
	}

}
