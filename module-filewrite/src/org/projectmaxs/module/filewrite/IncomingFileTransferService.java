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

package org.projectmaxs.module.filewrite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.aidl.IMAXSIncomingFileTransferService;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.ParcelFileDescriptorUtil;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public class IncomingFileTransferService extends Service {

	private static final Log LOG = Log.getLog();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IMAXSIncomingFileTransferService.Stub mBinder = new IMAXSIncomingFileTransferService.Stub() {

		@Override
		public ParcelFileDescriptor incomingFileTransfer(String filename, long size,
				String description) throws RemoteException {

			if (!GlobalConstants.MAXS_EXTERNAL_STORAGE.mkdirs()) {
				LOG.e("incomingFileTransfer: Could not create storage dir");
				return null;
			}

			final File inFile = new File(GlobalConstants.MAXS_EXTERNAL_STORAGE, filename);

			OutputStream os;
			try {
				os = new FileOutputStream(inFile);
			} catch (FileNotFoundException e) {
				LOG.e("incomingFileTransfer", e);
				return null;
			}

			ParcelFileDescriptor pfd;
			try {
				pfd = ParcelFileDescriptorUtil.pipeTo(os);
			} catch (IOException e) {
				LOG.e("incomingFileTransfer", e);
				return null;
			}
			return pfd;

		}
	};

	public void send(Message message) {
		Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_MESSAGE);
		replyIntent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
		startService(replyIntent);
	}
}
