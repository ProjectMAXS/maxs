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

import org.jivesoftware.smackx.iqversion.VersionManager;
import org.projectmaxs.transport.xmpp.R;

import android.content.Context;

public class XMPPVersion {

	public static void initialize(Context context) {
		String version = context.getString(R.string.version);
		String os = "Android " + android.os.Build.VERSION.RELEASE + " (API "
				+ android.os.Build.VERSION.SDK_INT + ')';
		VersionManager.setDefaultVersion("MAXS", version, os);
	}

}
