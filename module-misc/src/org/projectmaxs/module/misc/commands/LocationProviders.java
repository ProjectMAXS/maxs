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

package org.projectmaxs.module.misc.commands;

import java.util.List;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.commands.AbstractLocation;

public class LocationProviders extends AbstractLocation {

	public LocationProviders() {
		super("providers", true);
		setHelp(ArgType.NONE, "List the last known locations");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service) {
		super.execute(arguments, command, service);

		List<String> providers = mLocationManager.getAllProviders();

		Message message = new Message();
		Text text = new Text();
		text.addBoldNL("Location Providers");
		for (String provider : providers) {
			text.addNL("- " + provider);
		}
		message.add(text);

		return message;
	}
}
