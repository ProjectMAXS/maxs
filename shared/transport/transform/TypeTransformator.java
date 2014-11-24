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

package org.projectmaxs.shared.transport.transform;

import org.projectmaxs.shared.global.messagecontent.CommandHelp;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.messagecontent.ContactNumber;
import org.projectmaxs.shared.global.messagecontent.Sms;

public class TypeTransformator {

	private static String sMobile = "Mobile";
	private static String sHome = "Home";
	private static String sWork = "Work";
	private static String sUnknown = "Unknown";
	private static String sOther = "Other";

	private static String sFile = "file";
	private static String sPath = "path";
	private static String sNumber = "number";
	private static String sContactInfo = "contact info";
	private static String sContactNickname = "contact nickname";
	private static String sContactName = "contact name";

	public static String fromNumberType(ContactNumber.NumberType type) {
		switch (type) {
		case MOBILE:
			return sMobile;
		case HOME:
			return sHome;
		case WORK:
			return sWork;
		case OTHER:
			return sOther;
		default:
			return sUnknown;
		}
	}

	public static String fromSMSType(Sms.Type type) {
		switch (type) {
		case ALL:
			return "All";
		case INBOX:
			return "From";
		case SENT:
			return "To";
		case DRAFT:
			return "Draft";
		case OUTBOX:
			return "Outbox";
		case FAILED:
			return "Failed";
		case QUEUED:
			return "Queued";
		default:
			throw new IllegalStateException("Unknown sms type: " + type);
		}
	}

	public static StringBuilder toCommandArg(CommandHelp commandHelp) {
		StringBuilder sb = new StringBuilder();
		if (commandHelp.mArgType != ArgType.NONE) {
			sb.append(" <");
			switch (commandHelp.mArgType) {
			case FILE:
				sb.append(sFile);
				break;
			case PATH:
				sb.append(sPath);
				break;
			case NUMBER:
				sb.append(sNumber);
				break;
			case CONTACT_INFO:
				sb.append(sContactInfo);
				break;
			case CONTACT_NICKNAME:
				sb.append(sContactNickname);
				break;
			case CONTACT_NAME:
				sb.append(sContactName);
				break;
			case OTHER_STRING:
				sb.append(commandHelp.mArgString);
				break;
			default:
				throw new IllegalArgumentException("Unknown ArgType: " + commandHelp.mArgType);
			}
			sb.append('>');
		}
		return sb;
	}
}
