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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class DialogUtil {

	/**
	 * 
	 * @param message
	 *            the message explaining why the package should get installed
	 * @param packageToInstall
	 *            the package which should get installed
	 * @param context
	 */
	public static final void displayPackageInstallDialog(final String message,
			final String packageToInstall, final Context context) {
		final PackageManagerUtil packageManagerUtil = PackageManagerUtil.getInstance(context);
		final Uri MARKET_URI = Uri.parse("market://search?q=pname:" + packageToInstall);
		final Uri FDROID_URI = Uri.parse("fdroid.app:" + packageToInstall);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		// We don't set the negative button with cancel here, as it's not really necessary
		builder.setPositiveButton("Install from Play Store", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final Intent install = new Intent(Intent.ACTION_VIEW, MARKET_URI);
				if (packageManagerUtil.isIntentAvailable(install)) {
					context.startActivity(install);
				} else {
					Toast.makeText(context,
							"No handler for 'market://' links (e.g. Play Store) available.",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		builder.setNeutralButton("Install from F-Droid", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final Intent install = new Intent(Intent.ACTION_VIEW, FDROID_URI);
				if (packageManagerUtil.isIntentAvailable(install)) {
					context.startActivity(install);
				} else {
					Toast.makeText(context, "No handler for 'fdroid.app:' links available",
							Toast.LENGTH_LONG).show();
				}
			}
		});
		builder.show();
	}

}
