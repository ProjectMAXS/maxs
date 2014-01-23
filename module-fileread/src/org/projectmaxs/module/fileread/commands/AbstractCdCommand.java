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

package org.projectmaxs.module.fileread.commands;

import java.io.File;

import org.projectmaxs.module.fileread.ModuleService;
import org.projectmaxs.shared.global.Message;

public abstract class AbstractCdCommand extends AbstractFilereadCommand {

	public AbstractCdCommand(String name) {
		super(ModuleService.CD, name);
	}

	public AbstractCdCommand(String name, boolean isDefaultWithoutArguments,
			boolean isDefaultWithArguments) {
		super(ModuleService.CD, name, isDefaultWithoutArguments, isDefaultWithArguments);
	}

	final Message cd(File path) {
		Message message;
		if (path.isDirectory()) {
			mSettings.setCwd(path);
			message = new Message("Change working directory to: " + path.getAbsolutePath());
		} else {
			message = new Message("Not a directory: " + path.getAbsolutePath());
		}
		return message;
	}
}
