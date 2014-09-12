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

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.messagecontent.BooleanElement;

public class RadioOff extends AbstractRadioCommand {

	public RadioOff() {
		super("off", false, false);
		setHelp(ArgType.NONE, "Shut down the phones mobile radio");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Exception {
		super.execute(arguments, command, service);

		boolean res = telephonyService.setRadio(false);
		return new Message(BooleanElement.successOrUnsuccessfully(" disabled mobile radio",
				"mobile_radio_disabled", res));
	}
}
