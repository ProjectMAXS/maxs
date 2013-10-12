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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.projectmaxs.shared.global.aidl.IFileReadModuleService;
import org.projectmaxs.shared.global.util.Log;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class FileReadService extends Service {

	private static final Log LOG = Log.getLog();

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
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

		@Override
		public boolean isFile(String file) throws RemoteException {
			return new File(file).isFile();
		}
	};

}
