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

package org.projectmaxs.transport.xmpp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.MAXSIncomingFileTransfer;
import org.projectmaxs.shared.global.util.ParcelFileDescriptorUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelFileDescriptor;

public class XMPPFileTransfer extends StateChangeListener implements FileTransferListener {

	private static final Log LOG = Log.getLog();

	private final Settings mSettings;
	private final Context mContext;

	private FileTransferManager mFileTransferManager;

	protected XMPPFileTransfer(Settings settings, Context context) {
		mSettings = settings;
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

		String content = "foobar";
		InputStream is = new StringBufferInputStream(content);
		ParcelFileDescriptor pfd;
		try {
			pfd = ParcelFileDescriptorUtil.pipeFrom(is);
		} catch (IOException e) {
			LOG.e("fileTransferRequest", e);
			return;
		}

		MAXSIncomingFileTransfer mift = new MAXSIncomingFileTransfer("foo", content.length(), "bar", pfd, requestor);

		Intent intent = new Intent(GlobalConstants.ACTION_INCOMING_FILETRANSFER);
		intent.putExtra(GlobalConstants.EXTRA_CONTENT, mift);
		ComponentName cn = mContext.startService(intent);
		LOG.d(cn.toString());
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
