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

package org.projectmaxs.module.fileread.cmd;

import java.io.File;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.aidl.IMAXSOutgoingFileTransferService;
import org.projectmaxs.shared.global.util.AsyncServiceTask;

import android.content.Context;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;

public class FileCmd {

	public final static Message handleSend(Context context, String file) {
		final File toSend = new File(GlobalConstants.MAXS_EXTERNAL_STORAGE, file);

		/*
		new AsyncServiceTask<IMAXSOutgoingFileTransferService>(GlobalConstants.ACTION_OUTGOING_FILETRANSFER, context) {

			@Override
			public IMAXSOutgoingFileTransferService asInterface(IBinder iBinder) {
				return IMAXSOutgoingFileTransferService.Stub.asInterface(iBinder);
			}

			@Override
			public void performTask(IMAXSOutgoingFileTransferService iinterface) {
				ParcelFileDescriptor pfd = iinterface.outgoingFileTransfer(file, size, description, toJID);
			}

		}.go();
		*/
		return null;
	}
}
