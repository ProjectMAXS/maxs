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

package org.projectmaxs.module.notification;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.util.DateTimeUtil;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.module.MainUtil;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class MAXSNotificationListenerService extends NotificationListenerService {

	private static final Log LOG = Log.getLog();

	private Settings mSettings;

	@Override
	public void onCreate() {
		LOG.d("onCreate");
		mSettings = Settings.getInstance(this);
	}

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		LOG.d("onNotificationPosted: sbn=" + sbn);
		if (!mSettings.notifcationPosted()) return;

		Element element = new Element("notificationPosted", null, "New notification posted");
		addSbnToElement(sbn, element);

		Message message = new Message(element);
		MainUtil.send(message, this);
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		LOG.d("onNotificationRemoved: sbn=" + sbn);
		if (!mSettings.notifcationRemoved()) return;

		Element element = new Element("notificationRemoved", null, "Notifcation removed");
		addSbnToElement(sbn, element);

		Message message = new Message(element);
		MainUtil.send(message, this);
	}

	private static void addSbnToElement(StatusBarNotification sbn, Element element) {
		Notification notification = sbn.getNotification();

		if (notification != null && notification.tickerText != null)
			element.addChildElement(new Element("tickerText", notification.tickerText.toString(),
					"Ticker Text: " + notification.tickerText));
		element.addChildElement(new Element("packageName", sbn.getPackageName(), "Package: "
				+ sbn.getPackageName()));
		element.addChildElement(new Element("when", Long.toString(notification.when), "Posted: "
				+ DateTimeUtil.fullFromUtc(notification.when)));
	}
}
