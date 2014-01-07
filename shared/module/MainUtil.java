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

package org.projectmaxs.shared.module;

import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.mainmodule.MainModuleConstants;

import android.content.Context;
import android.content.Intent;

public class MainUtil {

	public static final void send(Message message, Context context) {
		Intent replyIntent = new Intent(GlobalConstants.ACTION_SEND_MESSAGE);
		replyIntent.setClassName(GlobalConstants.MAIN_PACKAGE,
				MainModuleConstants.MAIN_MODULE_SERVICE);
		replyIntent.putExtra(GlobalConstants.EXTRA_MESSAGE, message);
		context.startService(replyIntent);
	}

}
