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

package org.projectmaxs.main.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.projectmaxs.shared.global.messagecontent.CommandHelp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CommandHelpTable {

	private static final String TABLE_NAME = "commandHelp";
	private static final String COLUMN_NAME_PACKAGE = "package";
	private static final String COLUMN_NAME_COMMAND = "command";
	private static final String COLUMN_NAME_SUBCOMMAND = "subcommand";
	private static final String COLUMN_NAME_ARG_TYPE = "argType";
	private static final String COLUMN_NAME_ARG_STRING = "argString";
	private static final String COLUMN_NAME_HELP = "help";

	// @formatter:off
	public static final String CREATE_TABLE =
		"CREATE TABLE " +  TABLE_NAME +
		"(" +
		 COLUMN_NAME_PACKAGE + MAXSDatabase.TEXT_TYPE + MAXSDatabase.NOT_NULL + ',' +
		 COLUMN_NAME_COMMAND + MAXSDatabase.TEXT_TYPE + MAXSDatabase.NOT_NULL + ',' +
		 COLUMN_NAME_SUBCOMMAND + MAXSDatabase.TEXT_TYPE + MAXSDatabase.NOT_NULL + ',' +
		 COLUMN_NAME_ARG_TYPE + MAXSDatabase.INTEGER_TYPE + MAXSDatabase.NOT_NULL + ',' +
		 COLUMN_NAME_ARG_STRING + MAXSDatabase.TEXT_TYPE + ',' +
 		 COLUMN_NAME_HELP + MAXSDatabase.TEXT_TYPE + MAXSDatabase.NOT_NULL + ',' +
 		 " PRIMARY KEY (" + COLUMN_NAME_COMMAND + ',' + COLUMN_NAME_SUBCOMMAND + ')' +
		")";
	// @formatter:on

	public static final String DELETE_TABLE = MAXSDatabase.DROP_TABLE + TABLE_NAME;

	private static CommandHelpTable sCommandHelp;

	public static CommandHelpTable getInstance(Context context) {
		if (sCommandHelp == null) sCommandHelp = new CommandHelpTable(context);
		return sCommandHelp;
	}

	private final SQLiteDatabase mDatabase;

	private CommandHelpTable(Context context) {
		mDatabase = MAXSDatabase.getInstance(context).getWritableDatabase();
	}

	public void addCommandHelp(final String pkg, final Set<CommandHelp> commandHelpSet) {
		for (CommandHelp help : commandHelpSet) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_NAME_PACKAGE, pkg);
			values.put(COLUMN_NAME_COMMAND, help.mCommand);
			values.put(COLUMN_NAME_SUBCOMMAND, help.mSubCommand);
			values.put(COLUMN_NAME_ARG_TYPE, help.mArgType.ordinal());
			if (help.mArgType == CommandHelp.ArgType.OTHER_STRING)
				values.put(COLUMN_NAME_ARG_STRING, help.mArgString);
			values.put(COLUMN_NAME_HELP, help.mHelp);

			long res = mDatabase.replace(TABLE_NAME, null, values);
			if (res == -1) throw new IllegalStateException("Could not insert help into database");
		}
	}

	public List<CommandHelp> getHelp() {
		final String[] projection = { COLUMN_NAME_COMMAND, COLUMN_NAME_SUBCOMMAND,
				COLUMN_NAME_ARG_TYPE, COLUMN_NAME_HELP, COLUMN_NAME_ARG_STRING };
		Cursor c = mDatabase.query(TABLE_NAME, projection, null, null, null, null, null);
		if (!c.moveToFirst()) {
			c.close();
			return null;
		}
		List<CommandHelp> res = new ArrayList<CommandHelp>(c.getCount());
		do {
			CommandHelp.ArgType argType = CommandHelp.ArgType.values()[c.getInt(c
					.getColumnIndexOrThrow(COLUMN_NAME_ARG_TYPE))];
			String help = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_HELP));
			String command = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_COMMAND));
			String subCommand = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_SUBCOMMAND));

			if (argType == CommandHelp.ArgType.OTHER_STRING) {
				String argString = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_ARG_STRING));
				res.add(new CommandHelp(command, subCommand, argString, help));
			} else {
				res.add(new CommandHelp(command, subCommand, argType, help));
			}
		} while (c.moveToNext());
		c.close();
		return res;
	}

	public List<CommandHelp> getHelp(String command) {
		final String[] projection = { COLUMN_NAME_SUBCOMMAND, COLUMN_NAME_ARG_TYPE,
				COLUMN_NAME_HELP, COLUMN_NAME_ARG_STRING };
		Cursor c = mDatabase.query(TABLE_NAME, projection, COLUMN_NAME_COMMAND + "=?",
				new String[] { command }, null, null, null);
		if (!c.moveToFirst()) {
			c.close();
			return null;
		}
		List<CommandHelp> res = new ArrayList<CommandHelp>(c.getCount());
		do {
			CommandHelp.ArgType argType = CommandHelp.ArgType.values()[c.getInt(c
					.getColumnIndexOrThrow(COLUMN_NAME_ARG_TYPE))];
			String help = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_HELP));
			String subCommand = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_SUBCOMMAND));

			if (argType == CommandHelp.ArgType.OTHER_STRING) {
				String argString = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_ARG_STRING));
				res.add(new CommandHelp(command, subCommand, argString, help));
			} else {
				res.add(new CommandHelp(command, subCommand, argType, help));
			}
		} while (c.moveToNext());
		c.close();
		return res;
	}

	public CommandHelp getHelp(String command, String subCommand) {
		final String[] projection = { COLUMN_NAME_ARG_TYPE, COLUMN_NAME_HELP,
				COLUMN_NAME_ARG_STRING };
		Cursor c = mDatabase.query(TABLE_NAME, projection, COLUMN_NAME_COMMAND + " = ? AND "
				+ COLUMN_NAME_SUBCOMMAND + " = ?", new String[] { command, subCommand }, null,
				null, null);
		if (!c.moveToFirst()) {
			c.close();
			return null;
		}
		CommandHelp.ArgType argType = CommandHelp.ArgType.values()[c.getInt(c
				.getColumnIndexOrThrow(COLUMN_NAME_ARG_TYPE))];
		String help = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_HELP));

		if (argType == CommandHelp.ArgType.OTHER_STRING) {
			String argString = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_ARG_STRING));
			c.close();
			return new CommandHelp(command, subCommand, argString, help);
		} else {
			c.close();
			return new CommandHelp(command, subCommand, argType, help);
		}
	}

	public void deleteEntriesOf(String pkg) {
		mDatabase.delete(TABLE_NAME, COLUMN_NAME_PACKAGE + "=?", new String[] { pkg });
	}
}
