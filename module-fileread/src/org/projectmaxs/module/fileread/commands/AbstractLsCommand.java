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
import java.io.FileFilter;
import java.util.Arrays;

import org.projectmaxs.module.fileread.ModuleService;
import org.projectmaxs.shared.global.Message;

public abstract class AbstractLsCommand extends AbstractFilereadCommand {

	public AbstractLsCommand(String name, boolean isDefaultWithoutArguments,
			boolean isDefaultWithArguments) {
		super(ModuleService.LS, name, isDefaultWithoutArguments, isDefaultWithArguments);
	}

	final Message list(String path) {
		if (path == null) {
			return list(mSettings.getCwd());
		} else {
			return list(fileFrom(path));
		}
	}

	final Message list(File path) {
		Message message;
		if (path.isDirectory()) {
			message = new Message("Content of " + path.getAbsolutePath());
			mSettings.setCwd(path);
			File[] dirs = path.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			File[] files = path.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			});
			if (dirs.length > 0) {
				Arrays.sort(dirs);
				for (File d : dirs) {
					message.add(toElement(d));
				}
			}
			if (files.length > 0) {
				Arrays.sort(files);
				for (File f : files) {
					message.add(toElement(f));
				}
			}
		} else if (path.isFile()) {
			message = new Message(path.getAbsolutePath());
		} else {
			message = new Message("No such file or directory: " + path);
		}
		return message;
	}
}
