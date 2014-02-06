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

package org.projectmaxs.module.shell.commands;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.module.SubCommand;
import org.projectmaxs.shared.module.SupraCommand;
import org.sufficientlysecure.rootcommands.Shell;
import org.sufficientlysecure.rootcommands.command.SimpleCommand;

public abstract class AbstractShell extends SubCommand {

	public AbstractShell(SupraCommand supraCommand, String name, boolean isDefaultWithoutArguments,
			boolean isDefaultWithArguments) {
		super(supraCommand, name, isDefaultWithoutArguments, isDefaultWithArguments);
	}

	static final Message execute(Shell shell, String command) throws IOException, TimeoutException {
		SimpleCommand simpleCommand = new SimpleCommand(command);
		shell.add(simpleCommand).waitForFinish();

		String output = simpleCommand.getOutput();
		Message message = new Message(output);

		shell.close();

		return message;
	}
}
