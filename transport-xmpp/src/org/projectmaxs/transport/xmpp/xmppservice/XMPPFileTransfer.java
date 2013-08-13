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

package org.projectmaxs.transport.xmpp.xmppservice;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.aidl.IMAXSIncomingFileTransferService;
import org.projectmaxs.shared.global.util.AsyncServiceTask;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.Settings;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public class XMPPFileTransfer extends StateChangeListener implements FileTransferListener {

	private static final Log LOG = Log.getLog();

	private final Settings mSettings;
	private final Context mContext;

	private FileTransferManager mFileTransferManager;

	protected XMPPFileTransfer(Context context) {
		mSettings = Settings.getInstance(context);
		mContext = context;
	}

	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		String requestor = request.getRequestor();
		if (!mSettings.isMasterJID(requestor)) {
			LOG.w("File transfer from non master jid " + requestor);
		}
		request.reject();
		// IncomingFileTransfer ift = request.accept();
		// InputStream is = null;
		// try {
		// is = ift.recieveFile();
		// } catch (XMPPException e) {
		// LOG.e("fileTransferRequest", e);
		// return;
		// }
		//
		// String filename = request.getFileName();
		// long size = request.getFileSize();
		// String description = request.getDescription();

		final String content = "foobar";
		final String filename = "foo";
		final String description = "more foobar";
		InputStream is = new StringBufferInputStream(content);
		// ParcelFileDescriptor pfd1;
		// try {
		// pfd1 = ParcelFileDescriptorUtil.pipeFrom(is);
		// } catch (IOException e) {
		// LOG.e("fileTransferRequest", e);
		// return;
		// }

		ParcelFileDescriptor[] pfds;
		try {
			pfds = ParcelFileDescriptor.createPipe();
		} catch (IOException e1) {
			return;
		}
		final ParcelFileDescriptor readSide = pfds[0];
		final ParcelFileDescriptor writeSide = pfds[1];

		OutputStream os = new FileOutputStream(writeSide.getFileDescriptor());
		try {
			os.write(content.getBytes());
			os.flush();
		} catch (IOException e1) {
			return;
		}

		final long size = content.length();

		new AsyncServiceTask<IMAXSIncomingFileTransferService>(
				new Intent(GlobalConstants.ACTION_INCOMING_FILETRANSFER), mContext) {

			@Override
			public IMAXSIncomingFileTransferService asInterface(IBinder iBinder) {
				return IMAXSIncomingFileTransferService.Stub.asInterface(iBinder);
			}

			@Override
			public void performTask(IMAXSIncomingFileTransferService iinterface) {
				try {
					iinterface.incomingFileTransfer(filename, size, description, readSide);
				} catch (RemoteException e) {
					LOG.e("fileTransferRequest", e);
				}
			}

		}.go();
	}

	@Override
	public void connected(Connection connection) {
		mFileTransferManager = new FileTransferManager(connection);
		mFileTransferManager.addFileTransferListener(this);
	}

	@Override
	public void disconnected(Connection connection) {
		mFileTransferManager.removeFileTransferListener(this);
		mFileTransferManager = null;
	}
}
