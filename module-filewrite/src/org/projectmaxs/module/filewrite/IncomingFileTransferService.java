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

import java.io.OutputStream;

import org.projectmaxs.shared.GlobalConstants;
import org.projectmaxs.shared.MAXSIncomingFileTransfer;

import android.app.IntentService;
import android.content.Intent;

public class IncomingFileTransferService extends IntentService {

	public IncomingFileTransferService() {
		super("IncomingFileTransferService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		MAXSIncomingFileTransfer mift = intent.getParcelableExtra(GlobalConstants.EXTRA_CONTENT);

		String filename = mift.getFilename();
		OutputStream os = mift.getOutputStream();
	}
}
