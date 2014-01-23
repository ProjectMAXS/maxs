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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.projectmaxs.module.fileread.ModuleService;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.aidl.IMAXSOutgoingFileTransferService;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.global.util.AsyncServiceTask;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.mainmodule.MAXSContentProviderContract;
import org.projectmaxs.shared.module.MAXSModuleIntentService;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

public class SendPath extends AbstractFilereadCommand {

	private static final Log LOG = Log.getLog();

	public SendPath() {
		super(ModuleService.SEND, "path", false, true);
		setHelp(ArgType.FILE, "Send file");
	}

	@Override
	public Message execute(String arguments, Command command, final MAXSModuleIntentService service)
			throws Throwable {
		super.execute(arguments, command, service);

		final String file = command.getArgs();
		final File toSend = fileFrom(file);

		if (!toSend.isFile()) return new Message("Not a file: " + toSend);

		ContentResolver cr = service.getContentResolver();
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
				action, service) {
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
					service.send(new Message("Exception while sending file" + e.getMessage()));
					LOG.e("handleSend: performTask exception", e);
				} finally {
					try {
						if (is != null) is.close();
						if (os != null) os.close();
					} catch (IOException e) {}
				}
				service.removePendingAction(this);
			}
		};
		service.addPendingAction(ast);
		ast.go();

		return null;
	}

}
