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
import org.projectmaxs.shared.global.MAXSIncomingFileTransfer;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.Log;

import android.app.IntentService;
import android.content.Intent;

public class IncomingFileTransferService extends IntentService {

	private static final Log LOG = Log.getLog();

	public IncomingFileTransferService() {
		super("IncomingFileTransferService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		android.os.Debug.waitForDebugger();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		LOG.d("onHandleIntent");
		MAXSIncomingFileTransfer mift = intent.getParcelableExtra(GlobalConstants.EXTRA_CONTENT);

		String filename = mift.getFilename();
		InputStream is = mift.getInputStream();

		OutputStream os;
		try {
			os = new FileOutputStream(new File(GlobalConstants.MAXS_EXTERNAL_STORAGE, filename));
		} catch (FileNotFoundException e) {
			LOG.e("onHandleIntent", e);
			return;
		}

		int len;
		byte[] buf = new byte[1024];
		try {
			while ((len = is.read(buf)) > 0) {
				os.write(buf, 0, len);
			}
		} catch (IOException e) {
			LOG.e("onHandleIntent", e);
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

	public void sendMessage(Message message) {
		Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_USER_MESSAGE);
		replyIntent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
		startService(replyIntent);
	}
}
