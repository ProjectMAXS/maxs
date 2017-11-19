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
package org.projectmaxs.module.locationfine.commands;

import java.util.ArrayList;
import java.util.List;

import org.projectmaxs.module.locationfine.LocationUtil;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.AbstractElement;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SharedLocationUtil;
import org.projectmaxs.shared.module.commands.AbstractLocation;

import android.location.Location;

public class LocationLastKnown extends AbstractLocation {

	public LocationLastKnown() {
		super("lastknown", false);
		setHelp(ArgType.NONE, "List the last known locations by the different location providers");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service) {
		super.execute(arguments, command, service);

		List<Location> lastKnownLocations = LocationUtil.getLastKnownLocations(mLocationManager);
		List<AbstractElement> elements = new ArrayList<>((lastKnownLocations.size() * 2) + 1);

		Text text = new Text();
		text.addBoldNL("Last known Locations");
		elements.add(text);

		for (Location location : lastKnownLocations) {
			List<AbstractElement> locationElements = SharedLocationUtil.toElements(location);
			elements.addAll(locationElements);
		}

		Message message = new Message(elements);
		return message;
	}
}
