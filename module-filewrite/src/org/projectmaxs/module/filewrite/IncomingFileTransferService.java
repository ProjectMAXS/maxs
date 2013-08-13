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
import java.io.InputStream;
import java.io.OutputStream;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.aidl.IMAXSIncomingFileTransferService;
import org.projectmaxs.shared.global.util.Log;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public class IncomingFileTransferService extends Service {

	private static final Log LOG = Log.getLog();

	@Override
	public void onCreate() {
		super.onCreate();
		android.os.Debug.waitForDebugger();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IMAXSIncomingFileTransferService.Stub mBinder = new IMAXSIncomingFileTransferService.Stub() {

		@Override
		public void incomingFileTransfer(String filename, long size, String description, ParcelFileDescriptor pfd)
				throws RemoteException {

			InputStream is = new ParcelFileDescriptor.AutoCloseInputStream(pfd);
			OutputStream os;
			try {
				os = new FileOutputStream(new File(GlobalConstants.MAXS_EXTERNAL_STORAGE, filename));
			} catch (FileNotFoundException e) {
				LOG.e("incomingFileTransfer", e);
				return;
			}
			finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}

			int len;
			byte[] buf = new byte[1024];
			try {
				while ((len = is.read(buf)) > 0) {
					os.write(buf, 0, len);
				}
			} catch (IOException e) {
				LOG.e("incomingFileTransfer", e);
				return;
			}
			finally {
				try {
					is.close();
				} catch (IOException e) {
				}
				try {
					os.close();
				} catch (IOException e) {
				}
			}

			sendMessage(new Message("Received file " + filename));
		}
	};

	public void sendMessage(Message message) {
		Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_MESSAGE);
		replyIntent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
		startService(replyIntent);
	}
}
