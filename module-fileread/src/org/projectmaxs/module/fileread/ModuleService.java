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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.aidl.IFileReadModuleService;
import org.projectmaxs.shared.global.aidl.IMAXSOutgoingFileTransferService;
import org.projectmaxs.shared.global.messagecontent.Element;
import org.projectmaxs.shared.global.messagecontent.Text;
import org.projectmaxs.shared.global.util.AsyncServiceTask;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.MAXSContentProviderContract;
import org.projectmaxs.shared.mainmodule.ModuleInformation;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.UnkownCommandException;
import org.projectmaxs.shared.module.UnkownSubcommandException;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
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
							"send",                    // Command name
							null,                      // Short command name
							null,                      // Default subcommand without arguments
							"path",                    // Default subcommand with arguments
							new String[] { "path" }),  // Array of provided subcommands 
					new ModuleInformation.Command(
							"ls",                          // Command name
							null,                          // Short command name
							"~",                           // Default subcommnd without arguments
							"path",                        // Default subcommand with arguments
							new String[] { "~", "path" }), // Array of provided subcommands
					new ModuleInformation.Command(
							"cd",
							null,
							"~",
							"path",
							new String[] { "~", "path" }),
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

		if ("send".equals(cmd)) {
			if ("path".equals(subCommand)) {
				message = send(command);
			} else {
				throw new UnkownSubcommandException(command);
			}
		} else if ("ls".equals(cmd)) {
			if ("path".equals(subCommand)) {
				message = list(command.getArgs());
			} else if ("~".equals(subCommand)) {
				message = list(mSettings.getCwd());
			} else {
				throw new UnkownSubcommandException(command);
			}
		} else if ("cd".equals(cmd)) {
			if ("~".equals(subCommand)) {
				message = cd(GlobalConstants.MAXS_EXTERNAL_STORAGE);
			} else if ("path".equals(subCommand)) {
				message = cd(fileFrom(command.getArgs()));
			} else {
				throw new UnkownSubcommandException(command);
			}
		} else {
			throw new UnkownCommandException(command);
		}
		return message;
	}

	@Override
	public void initLog(Context context) {
		LOG.initialize(Settings.getInstance(context));
	}

	private final Message list(String path) {
		if (path == null) {
			return list(mSettings.getCwd());
		} else {
			return list(fileFrom(path));
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
					message.add(toElement(d));
				}
			}
			if (files.length > 0) {
				Arrays.sort(files);
				for (File f : files) {
					message.add(toElement(f));
				}
			}
		} else {
			message = new Message("TODO");
		}
		return message;
	}

	private final Message send(Command command) {
		final String file = command.getArgs();
		final File toSend = fileFrom(file);

		if (!toSend.isFile()) return new Message("Not a file: " + toSend);

		ContentResolver cr = getContentResolver();
		Uri uri = ContentUris.withAppendedId(
				MAXSContentProviderContract.OUTGOING_FILE_TRANSFER_URI, (long) command.getId());
		Cursor c = cr.query(uri, null, null, null, null);
		if (!c.moveToFirst()) throw new IllegalStateException("Empty cursor");
		final String action = c.getString(c
				.getColumnIndexOrThrow(MAXSContentProviderContract.OUTGOING_FILETRANSFER_SERVICE));
		final String receiver = c.getString(c
				.getColumnIndexOrThrow(MAXSContentProviderContract.RECEIVER_INFO));
		c.close();

		AsyncServiceTask<IMAXSOutgoingFileTransferService> ast = new AsyncServiceTask<IMAXSOutgoingFileTransferService>(
				action, this) {
			@Override
			public IMAXSOutgoingFileTransferService asInterface(IBinder iBinder) {
				return IMAXSOutgoingFileTransferService.Stub.asInterface(iBinder);
			}

			@Override
			public void performTask(IMAXSOutgoingFileTransferService iinterface) {
				InputStream is = null;
				OutputStream os = null;
				try {
					ParcelFileDescriptor pfd = iinterface.outgoingFileTransfer(toSend.getName(),
							toSend.length(), toSend.getAbsolutePath(), receiver);

					int len;
					byte[] buf = new byte[1024];

					is = new FileInputStream(toSend);
					os = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);
					while ((len = is.read(buf)) > 0) {
						os.write(buf, 0, len);
					}

				} catch (Exception e) {
					send(new Message("Exception while sending file" + e.getMessage()));
					LOG.e("handleSend: performTask exception", e);
				} finally {
					try {
						if (is != null) is.close();
						if (os != null) os.close();
					} catch (IOException e) {}
				}
				removePendingAction(this);
			}
		};
		addPendingAction(ast);
		ast.go();

		return null;
	}

	private final Message cd(File path) {
		Message message;
		if (path.isDirectory()) {
			mSettings.setCwd(path);
			message = new Message("Change working directory to: " + path.getAbsolutePath());
		} else {
			message = new Message("Not a directory: " + path.getAbsolutePath());
		}
		return message;
	}

	private final File fileFrom(String path) {
		if (path.startsWith("/")) {
			return new File(path);
		} else {
			return new File(mSettings.getCwd(), path);
		}
	}

	private final IFileReadModuleService.Stub mBinder = new IFileReadModuleService.Stub() {

		@Override
		public byte[] readFileBytes(String file) throws RemoteException {
			final File readFrom = new File(file);
			if (!readFrom.isFile()) {
				LOG.e("readFileBytes: not a file " + file);
				return null;
			}
			if (readFrom.length() > Integer.MAX_VALUE) {
				// Even if the file had a size of Integer.MAX_VALUE, it would be to big.
				LOG.e("readFileBytes: file is to big");
				return null;
			}

			int len = 0;
			byte[] buf = new byte[1024];
			ByteArrayOutputStream os = null;
			InputStream is = null;
			try {
				os = new ByteArrayOutputStream((int) readFrom.length());
				is = new FileInputStream(readFrom);
				while ((len = is.read(buf)) != -1) {
					os.write(buf, 0, len);
				}
			} catch (Exception e) {
				LOG.e("readFileBytes", e);
				return null;
			} finally {
				if (os != null) try {
					os.close();
				} catch (IOException e) {}
				if (is != null) try {
					is.close();
				} catch (IOException e) {}
			}
			return os.toByteArray();
		}
	};

	private static final Element toElement(File file) {
		final String path = file.getAbsolutePath();
		Element element;
		if (file.isDirectory()) {
			element = new Element("directory", file.getAbsolutePath(), path + '/');
		} else {
			final long size = file.length();
			Text text = new Text(path);
			element = new Element("file", file.getAbsolutePath(), text);
			element.addChildElement(new Element("size", String.valueOf(size)));
		}
		return element;
	}

}
