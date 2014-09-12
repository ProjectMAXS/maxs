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

import org.projectmaxs.module.phonestatemodify.ModuleService;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.messagecontent.BooleanElement;

import com.android.internal.telephony.PhoneConstants;

public class PinSupply extends AbstractPhonestateModifyCommand {

	public PinSupply() {
		super(ModuleService.sPIN, "supply", false, true);
		setHelp(ArgType.OTHER_STRING, "Unlock the SIM by supplying a PIN");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Exception {
		super.execute(arguments, command, service);

		int[] res = telephonyService.supplyPinReportResult(arguments);
		boolean success;
		switch (res[0]) {
		case PhoneConstants.PIN_RESULT_SUCCESS:
			success = true;
			break;
		default:
			success = false;
			break;
		}

		return new Message(BooleanElement.trueOrFalse("Successfully unlock SIM",
				"Could not unlock SIM, " + res[1] + " retries left", "phone_sim_unlocked", success));
	}
}
