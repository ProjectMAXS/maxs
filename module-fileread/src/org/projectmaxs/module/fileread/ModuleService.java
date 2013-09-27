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

package org.projectmaxs.module.fileread;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

import org.projectmaxs.module.fileread.cmd.FileCmd;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.aidl.IFileReadModuleService;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.UnkownCommandException;
import org.projectmaxs.shared.module.UnkownSubcommandException;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class ModuleService extends MAXSModuleIntentService {
	private final static Log LOG = Log.getLog();

	private final Settings mSettings;

	public ModuleService() {
		super(LOG, "maxs-module-fileread");
		mSettings = Settings.getInstance(this);
	}

	// @formatter:off
	public static final ModuleInformation sMODULE_INFORMATION = new ModuleInformation(
			"org.projectmaxs.module.fileread",        // Package of the Module
			"fileread",                               // Name of the Module (if omitted, last substring after '.' is used)
			new ModuleInformation.Command[] {         // Array of commands provided by the module
					new ModuleInformation.Command(
							"file",                    // Command name
							"f",                       // Short command name
							null,                      // Default subcommand without arguments
							"send",                    // Default subcommand with arguments
							new String[] { "send" }),  // Array of provided subcommands 
					new ModuleInformation.Command(
							"ls",                      // Command name
							null,                      // Short command name
							null,                      // Default subcommnd without arguments
							"show",                    // Default subcommand with arguments
							new String[] { "show" }),  // Array of provided subcommands
			});
	// @formatter:on

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public Message handleCommand(Command command) {
		final String cmd = command.getCommand();
		final String subCommand = command.getSubCommand();
		Message message = null;

		if ("file".equals(cmd)) {
			if ("send".equals(subCommand)) {
				String file = command.getArgs();
				message = FileCmd.handleSend(this, file);
			} else {
				throw new UnkownSubcommandException(command);
			}
		} else if ("ls".equals(cmd)) {
			if ("show".equals(subCommand)) {
				message = list(command.getArgs());
			} else {
				throw new UnkownSubcommandException(command);
			}
		} else {
			throw new UnkownCommandException(command);
		}
		return null;
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}

	private final IFileReadModuleService.Stub mBinder = new IFileReadModuleService.Stub() {

		@Override
		public byte[] readFileBytes(String file) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}
	};

	private final Message list(String path) {
		if (path == null) {
			return list(mSettings.getCwd());
		} else if (path.startsWith("/")) {
			return list(new File(path));
		} else {
			return list(new File(mSettings.getCwd(), path));
		}
	}

	private final Message list(File path) {
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
					// TODO
				}
			}
			if (files.length > 0) {
				Arrays.sort(files);
				for (File f : files) {
					// TODO
				}
			}
		} else {
			message = new Message("TODO");
		}
		return message;
	}
}
