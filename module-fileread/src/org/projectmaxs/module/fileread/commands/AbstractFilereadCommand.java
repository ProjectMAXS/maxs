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

import org.projectmaxs.module.fileread.Settings;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SubCommand;
import org.projectmaxs.shared.module.SupraCommand;

public abstract class AbstractFilereadCommand extends SubCommand {

	Settings mSettings;

	public AbstractFilereadCommand(SupraCommand supraCommand, String name) {
		super(supraCommand, name);
	}

	public AbstractFilereadCommand(SupraCommand supraCommand, String name,
			boolean isDefaultWithoutArguments, boolean isDefaultWithArguments) {
		super(supraCommand, name, isDefaultWithoutArguments, isDefaultWithArguments);
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		mSettings = Settings.getInstance(service);

		return null;
	}

	final File fileFrom(String path) {
		if (path.startsWith("/")) {
			return new File(path);
		} else {
			return new File(mSettings.getCwd(), path);
		}
	}

	static final Element toElement(File file) {
		final String path = file.getAbsolutePath();
		Element element;
		if (file.isDirectory()) {
			element = new Element("directory", file.getAbsolutePath(), path + '/');
		} else {
			final long size = file.length();
			Text text = new Text(path + " " + SharedStringUtil.humandReadableByteCount(size));
			element = new Element("file", file.getAbsolutePath(), text);
			element.addChildElement(new Element("size", String.valueOf(size)));
		}
		return element;
	}
}
