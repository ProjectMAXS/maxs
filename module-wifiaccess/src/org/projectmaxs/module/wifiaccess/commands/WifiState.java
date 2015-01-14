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

package org.projectmaxs.module.wifiaccess.commands;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;

import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiState extends AbstractWifi {

	private static final int RSSI_NUM_LEVELS = 10;

	public WifiState() {
		super("state", true);
		setHelp(ArgType.NONE,
				"Display the state of the WiFi adapter including network and IP information");
	}

	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		super.execute(arguments, command, service);

		Message msg = new Message();
		int state = mWifiManager.getWifiState();
		msg.add(new Element("wifiState", Integer.toString(state), "Wifi state is "
				+ stateIntToString(state)));

		boolean supplicantResponding = mWifiManager.pingSupplicant();
		String supplicantText;
		if (supplicantResponding) {
			supplicantText = "Supplicant is responding to requests";
		} else {
			supplicantText = "Supplicant is NOT responding to requests";
		}
		msg.add(new Element("supplicantResponding", Boolean.toString(supplicantResponding),
				supplicantText));

		WifiInfo info = mWifiManager.getConnectionInfo();
		if (info != null) {
			Element wifiInfo = new Element("wifiInfo", null, "Connected WiFi information");

			String bssid = info.getBSSID();
			wifiInfo.addChildElement(new Element("bssid", bssid, "BSSID: " + bssid));

			String ssid = info.getSSID();
			wifiInfo.addChildElement(new Element("ssid", ssid, "SSID: " + ssid));

			String ip = SharedStringUtil.ipIntToString(info.getIpAddress());
			wifiInfo.addChildElement(new Element("ip", ip, "IP: " + ip));

			String linkSpeed = Integer.toString(info.getLinkSpeed());
			wifiInfo.addChildElement(new Element("linkSpeed", linkSpeed, "Link speed: " + linkSpeed
					+ WifiInfo.LINK_SPEED_UNITS));
			wifiInfo.addChildElement(Element.newNonHumandReadable("linkSpeedUnits",
					WifiInfo.LINK_SPEED_UNITS));

			int rssiInt = info.getRssi();
			String rssi = Integer.toString(rssiInt);
			wifiInfo.addChildElement(new Element("rssi", rssi,
					"Received singal strength indicator: " + rssi));

			String rssiLevel = Integer.toString(WifiManager.calculateSignalLevel(rssiInt,
					RSSI_NUM_LEVELS));
			wifiInfo.addChildElement(new Element("rssiLevel", rssiLevel,
					"RSSI on a scale from 1 to " + RSSI_NUM_LEVELS + ": " + rssiLevel));

			msg.add(wifiInfo);
		}

		DhcpInfo dhcpInfo = mWifiManager.getDhcpInfo();
		if (dhcpInfo != null) {
			Element e = new Element("dhcpInfo", null, "DHCP Info");

			String dns1 = SharedStringUtil.ipIntToString(dhcpInfo.dns1);
			e.addChildElement(new Element("dns1", dns1, "DNS1: " + dns1));

			String dns2 = SharedStringUtil.ipIntToString(dhcpInfo.dns2);
			e.addChildElement(new Element("dns2", dns2, "DNS2: " + dns2));

			String gateway = SharedStringUtil.ipIntToString(dhcpInfo.gateway);
			e.addChildElement(new Element("gateway", gateway, "Gateway: " + gateway));

			String ip = SharedStringUtil.ipIntToString(dhcpInfo.ipAddress);
			e.addChildElement(new Element("ip", ip, "IP: " + ip));

			String netmask = SharedStringUtil.ipIntToString(dhcpInfo.netmask);
			e.addChildElement(new Element("netmask", netmask, "Netmask: " + netmask));

			String leaseDuration = Integer.toString(dhcpInfo.leaseDuration);
			e.addChildElement(new Element("leaseDuration", leaseDuration, "Lease duration: "
					+ leaseDuration));

			String dhcpServerIp = SharedStringUtil.ipIntToString(dhcpInfo.serverAddress);
			e.addChildElement(new Element("dhcpServerIp", dhcpServerIp, "DHCP Server IP: "
					+ dhcpServerIp));

			msg.add(e);
		}

		return msg;
	}

	private static final String stateIntToString(int stateInt) {
		String status;
		switch (stateInt) {
		case WifiManager.WIFI_STATE_DISABLED:
			status = "disabled";
			break;
		case WifiManager.WIFI_STATE_DISABLING:
			status = "disabling";
			break;
		case WifiManager.WIFI_STATE_ENABLED:
			status = "enabled";
			break;
		case WifiManager.WIFI_STATE_ENABLING:
			status = "enabling";
			break;
		default:
			status = "unknown";
			break;
		}
		return status;
	}

}
