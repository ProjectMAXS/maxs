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

package org.projectmaxs.shared.global.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.ParcelFileDescriptor;

public class ParcelFileDescriptorUtil {

	private static final Log LOG = Log.getLog();

	public static ParcelFileDescriptor pipeFrom(InputStream inputStream) throws IOException {
		ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
		ParcelFileDescriptor readSide = pipe[0];
		ParcelFileDescriptor writeSide = pipe[1];

		// start the transfer thread
		new TransferThread(inputStream, new ParcelFileDescriptor.AutoCloseOutputStream(writeSide))
				.start();

		return readSide;
	}

	public static ParcelFileDescriptor pipeTo(OutputStream outputStream) throws IOException {
		ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
		ParcelFileDescriptor readSide = pipe[0];
		ParcelFileDescriptor writeSide = pipe[1];

		// start the transfer thread
		new TransferThread(new ParcelFileDescriptor.AutoCloseInputStream(readSide), outputStream)
				.start();

		return writeSide;
	}

	static class TransferThread extends Thread {
		final InputStream mIn;
		final OutputStream mOut;

		TransferThread(InputStream in, OutputStream out) {
			super("ParcelFileDescriptor Transfer Thread");
			mIn = in;
			mOut = out;
			setDaemon(true);
		}

		@Override
		public void run() {
			byte[] buf = new byte[1024];
			int len;

			try {
				while ((len = mIn.read(buf)) > 0) {
					mOut.write(buf, 0, len);
				}
				mOut.flush(); // just to be safe
			} catch (IOException e) {
				LOG.e("TransferThread", e);
			} finally {
				try {
					mIn.close();
				} catch (IOException e) {
					LOG.e("run", e);
				}
				try {
					mOut.close();
				} catch (IOException e) {
					LOG.e("run", e);
				}
			}
		}
	}
}
