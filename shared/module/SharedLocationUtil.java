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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.AbstractElement;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.global.util.DateTimeUtil;

import android.location.Location;
import android.os.Build;

public class SharedLocationUtil {

	public static List<AbstractElement> toElements(Location location) {
		List<AbstractElement> res = new ArrayList<>(2);

		String latitude = Double.toString(location.getLatitude());
		String longitude = Double.toString(location.getLongitude());
		String time = Long.toString(location.getTime());
		String humanTime = DateTimeUtil.shortFromUtc(location.getTime());

		String accuracy = location.hasAccuracy() ? Float.toString(location.getAccuracy()) : null;
		String altitude = location.hasAltitude() ? Double.toString(location.getAltitude()) : null;
		String speed = location.hasSpeed() ? Float.toString(location.getSpeed()) : null;

		Text text = new Text();
		text.addBoldNL("Location (" + humanTime + ", provider: " + location.getProvider() + ')');
		text.addNL("Latitude: " + latitude + " Longitude: " + longitude);
		text.addNL("https://www.openstreetmap.org/?mlat=" + latitude + "&mlon=" + longitude
				+ "&zoom=14&layers=M");
		if (accuracy != null) text.addNL("Accuracy: " + accuracy + " meters");
		if (altitude != null) text.addNL("Altitude: " + altitude + " meters");
		if (speed != null) text.addNL("Speed: " + speed + " meters/second");
		res.add(text);

		// Add a non human-readable element with that information
		Element element = new Element("location");
		element.addChildElement(new Element("latitude", latitude));
		element.addChildElement(new Element("longitude", longitude));
		element.addChildElement(new Element("time", time));
		element.addChildElement(new Element("provider", location.getProvider()));
		if (accuracy != null) element.addChildElement(new Element("accuracy", accuracy));
		if (altitude != null) element.addChildElement(new Element("altitude", altitude));
		if (speed != null) element.addChildElement(new Element("speed", speed));
		res.add(element);

		return res;
	}

	public static Message toMessage(Location location) {
		Message message = new Message();
		List<AbstractElement> elements = toElements(location);
		message.addAll(elements);
		return message;
	}

	public static int compareLocations(Location lhs, Location rhs) {
		long timeDelta;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			timeDelta = lhs.getElapsedRealtimeNanos() - rhs.getElapsedRealtimeNanos();
		} else {
			timeDelta = lhs.getTime() - rhs.getTime();
		}

		if (timeDelta > 1000 * 60 * 3) {
			if (Math.abs(timeDelta) > Integer.MAX_VALUE) {
				if (timeDelta > 0) {
					timeDelta = Integer.MAX_VALUE;
				} else {
					timeDelta = Integer.MIN_VALUE;
				}
			}
			return (int) timeDelta;
		}

		float accuracyDelta = (lhs.getAccuracy() - rhs.getAccuracy()) * 1000;
		return (int) accuracyDelta;
	}

	public static void sort(List<Location> locations) {
		Collections.sort(locations, new Comparator<Location>() {
			@Override
			public int compare(Location lhs, Location rhs) {
				return compareLocations(lhs, rhs);
			}
		});
	}
}
