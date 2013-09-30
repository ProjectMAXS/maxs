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

package org.projectmaxs.module.ringermode;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.UnkownCommandException;
import org.projectmaxs.shared.module.UnkownSubcommandException;

import android.content.Context;
import android.media.AudioManager;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	private AudioManager mAudioManager;

	public ModuleService() {
		super(LOG, "maxs-module-ringermode");
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.ringermode",      // Package of the Module
			"ringermode",                             // Name of the Module (if omitted, last substring after '.' is used)
			new ModuleInformation.Command[] {        // Array of commands provided by the module
					new ModuleInformation.Command(
							"ringermode",             // Command name
							"ringer",                    // Short command name
							"show",                // Default subcommand without arguments
							null,                    // Default subcommand with arguments
							new String[] { "normal", "silent", "vibrate", "show" }),  // Array of provided subcommands 
			});
	// @formatter:on

	@Override
	public void onCreate() {
		super.onCreate();
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}

	@Override
	public Message handleCommand(Command command) {
		final String cmd = command.getCommand();
		final String subCmd = command.getSubCommand();

		final Message msg;
		if ("ringermode".equals(cmd) || "ringer".equals(cmd)) {
			if ("show".equals(subCmd)) {
				msg = new Message("Ringer is in " + ringerMode() + " mode");
			} else if ("normal".equals(subCmd)) {
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				msg = new Message("Ringer set to normal");
			} else if ("silent".equals(subCmd)) {
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				msg = new Message("Ringer set to silent");
			} else if ("vibrate".equals(subCmd)) {
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				msg = new Message("Ringer set to vibrate");
			} else {
				throw new UnkownSubcommandException(command);
			}
		} else {
			throw new UnkownCommandException(command);
		}
		return msg;
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
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
