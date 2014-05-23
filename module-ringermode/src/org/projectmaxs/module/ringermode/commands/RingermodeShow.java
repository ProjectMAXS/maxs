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

package org.projectmaxs.module.ringermode.commands;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;

import android.media.AudioManager;

public class RingermodeShow extends AbstractRingermodeCommand {

	public RingermodeShow() {
		super("show", true, false);
		setHelp(ArgType.NONE, "Show the current mode of the ringer");
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service)
			throws Throwable {
		super.execute(arguments, command, service);

		return new Message("Ringer is in " + ringerMode() + " mode");
	}

	private String ringerMode() {
		int mode = mAudioManager.getRingerMode();
		switch (mode) {
		case AudioManager.RINGER_MODE_NORMAL:
			return "normal";
		case AudioManager.RINGER_MODE_SILENT:
			return "silent";
		case AudioManager.RINGER_MODE_VIBRATE:
			return "vibrate";
		default:
			throw new IllegalStateException();
		}
	}
}
