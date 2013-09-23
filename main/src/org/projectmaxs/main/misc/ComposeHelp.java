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

package org.projectmaxs.main.misc;

import java.util.List;

import org.projectmaxs.main.database.CommandHelpTable;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp;

import android.content.Context;

public class ComposeHelp {

	/**
	 * Composes a full list of known help if command and subCommand is null. If only command is
	 * given, composes a list of all known help for the given command. If both are given, only the
	 * help of the specific command will be returned.
	 * 
	 * @param command
	 * @param subCommand
	 * @param context
	 * @return
	 */
	public final static Message getHelp(String command, String subCommand, Context context) {
		Message msg;
		if (command == null && subCommand == null) {
			msg = getFullHelp(context);
		} else if (command != null && subCommand == null) {
			msg = getHelpFor(command, context);
		} else {
			msg = getHelpFor(command, subCommand, context);
		}
		return msg;
	}

	private final static Message getFullHelp(Context context) {
		Message msg;
		CommandHelpTable commandHelpTable = CommandHelpTable.getInstance(context);
		List<CommandHelp> help = commandHelpTable.getHelp();
		if (help == null) {
			msg = new Message("No help available so far");
		} else {
			msg = new Message();
			for (CommandHelp ch : help)
				msg.add(ch);
		}
		return msg;
	}

	private final static Message getHelpFor(String command, Context context) {
		Message msg;
		CommandHelpTable commandHelpTable = CommandHelpTable.getInstance(context);
		List<CommandHelp> help = commandHelpTable.getHelp(command);
		if (help == null) {
			msg = new Message("No help available for command: " + command);
		} else {
			msg = new Message();
			for (CommandHelp ch : help)
				msg.add(ch);
		}
		return msg;
	}

	private final static Message getHelpFor(String command, String subCommand, Context context) {
		Message msg;
		CommandHelpTable commandHelpTable = CommandHelpTable.getInstance(context);
		CommandHelp help = commandHelpTable.getHelp(command, subCommand);
		if (help == null) {
			msg = new Message("No help avail for command: " + command + ' ' + subCommand);
		} else {
			msg = new Message();
			msg.add(help);
		}
		return msg;
	}
}
