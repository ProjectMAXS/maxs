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

package org.projectmaxs.module.phonestatemodify.commands;

import java.lang.reflect.Method;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.PackageManagerUtil;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SubCommand;
import org.projectmaxs.shared.module.SupraCommand;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

public abstract class AbstractPhonestateModifyCommand extends SubCommand {

	public AbstractPhonestateModifyCommand(SupraCommand supraCommand, String name,
			boolean isDefaultWithoutArguments) {
		super(supraCommand, name, isDefaultWithoutArguments);
	}

	public AbstractPhonestateModifyCommand(SupraCommand supraCommand, String name,
			boolean isDefaultWithoutArguments, boolean isDefaultWithArguments) {
		super(supraCommand, name, isDefaultWithoutArguments, isDefaultWithArguments);
	}

	protected TelephonyManager telephonyManager;
	protected ITelephony telephonyService;

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Exception {
		if (!PackageManagerUtil.getInstance(service).isSystemApp()) {
			throw new Exception(
					"MAXS Module PhonestateModify needs to be an system app. For information on how to convert it to an system app see projectmaxs.org/systemapp");
		}

		telephonyManager = (TelephonyManager) service.getSystemService(Context.TELEPHONY_SERVICE);
		Class<?> c = Class.forName(telephonyManager.getClass().getName());
		Method m = c.getDeclaredMethod("getITelephony");
		m.setAccessible(true);
		telephonyService = (ITelephony) m.invoke(telephonyManager);

		return null;
	}
}
