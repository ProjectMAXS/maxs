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

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp;
import org.projectmaxs.shared.mainmodule.Command;

import android.content.Context;

public abstract class SubCommand {

	final SupraCommand mSupraCommand;
	final String mSubCommandName;

	final boolean mIsDefaultWithArguments;
	final boolean mIsDefaultWithoutArguments;

	private boolean mRequiresArgument = false;

	private String mArgString;
	private CommandHelp.ArgType mArgType;

	private int mHelpResId;
	private String mHelp;

	private CommandHelp mCommandHelp;

	public SubCommand(SupraCommand supraCommand, String name) {
		this(supraCommand, name, false, false);
	}

	public SubCommand(SupraCommand supraCommand, String name, boolean isDefaultWithoutArguments) {
		this(supraCommand, name, isDefaultWithoutArguments, false);
	}

	public SubCommand(SupraCommand supraCommand, String name, boolean isDefaultWithoutArguments,
			boolean isDefaultWithArguments) {
		mSupraCommand = supraCommand;
		mSubCommandName = name;
		mIsDefaultWithoutArguments = isDefaultWithoutArguments;
		mIsDefaultWithArguments = isDefaultWithArguments;
	}

	public CommandHelp getCommandHelp(Context context) {
		if (mCommandHelp == null) {
			String help;
			if (mHelpResId > 0 && mHelp == null) {
				help = context.getString(mHelpResId);
			} else if (mHelpResId <= 0 && mHelp != null) {
				help = mHelp;
			} else if (mHelpResId > 0 && mHelp != null) {
				throw new IllegalStateException("Must have either help resource ID or String");
			} else {
				// No help available for this sub command
				return null;
			}

			if (mArgString == null && mArgType != null) {
				mCommandHelp = new CommandHelp(mSupraCommand.getCommand(), mSubCommandName,
						mArgType, help);
			} else if (mArgString != null && mArgType == null) {
				mCommandHelp = new CommandHelp(mSupraCommand.getCommand(), mSubCommandName,
						mArgString, help);
			} else {
				throw new IllegalStateException("Must have arg type either as string or type enum");
			}
		}
		return mCommandHelp;
	}

	public String getSubCommandName() {
		return mSubCommandName;
	}

	public boolean requiresArgument() {
		return mRequiresArgument;
	}

	protected void setHelp(String argString, String help) {
		mArgString = argString;
		mHelp = help;
	}

	protected void setHelp(CommandHelp.ArgType type, String help) {
		mArgType = type;
		mHelp = help;
	}

	protected void setHelp(String argString, int helpResId) {
		mArgString = argString;
		mHelpResId = helpResId;
	}

	protected void setHelp(CommandHelp.ArgType type, int helpResId) {
		mArgType = type;
		mHelpResId = helpResId;
	}

	protected void setRequiresArgument() {
		mRequiresArgument = true;
	}

	public abstract Message execute(String arguments, Command command,
			MAXSModuleIntentService service) throws Throwable;
}
