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

package org.projectmaxs.shared.transport;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.TextView;

public class AndroidDozeUtil {

	private static final int DO_WHITELIST = 1;
	private static final int DO_NOT_WHITELIST = DO_WHITELIST + 1;
	private static final int ASK_AGAIN = DO_NOT_WHITELIST + 1;

	private static final String WHITELIST_DECISSION_KEY = "WHITELIST_DECISSION";

	public static void requestWhitelistIfNecessary(final Activity activity,
			final SharedPreferences sharedPreferences, final int resDozeAskForWhitelist,
			final int resDozeDoNotWhitelist, final int resAskAgain, final int resDozeWhitelist) {
		if (!isSupported()) {
			return;
		}

		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				requestWhitelistIfNecessary((Context) activity, sharedPreferences,
						resDozeAskForWhitelist, resDozeDoNotWhitelist, resAskAgain,
						resDozeWhitelist);
			}
		});
	}

	@TargetApi(23)
	public static void requestWhitelistIfNecessary(final Context context,
			final SharedPreferences sharedPreferences, final int resDozeAskForWhitelist,
			final int resDozeDoNotWhitelist, final int resAskAgain, final int resDozeWhitelist) {
		if (!isSupported()) {
			return;
		}

		final String myPackage = context.getPackageName();
		PowerManager powerManager = context.getSystemService(PowerManager.class);
		if (powerManager.isIgnoringBatteryOptimizations(myPackage)) {
			// We are already whitelisted.
			return;
		}

		if (sharedPreferences != null) {
			final int whitelistDecission = sharedPreferences.getInt(WHITELIST_DECISSION_KEY,
					ASK_AGAIN);
			switch (whitelistDecission) {
			case DO_WHITELIST:
				requestWhitelist(context, myPackage);
				break;
			case DO_NOT_WHITELIST:
				return;
			case ASK_AGAIN:
				final TextView textView = new TextView(context);
				textView.setText(resDozeAskForWhitelist);
				new AlertDialog.Builder(context).setView(textView)
						.setPositiveButton(resDozeWhitelist, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								sharedPreferences.edit()
										.putInt(WHITELIST_DECISSION_KEY, DO_WHITELIST).commit();
								requestWhitelist(context, myPackage);
							}
						}).setNeutralButton(resAskAgain, new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								sharedPreferences.edit().putInt(WHITELIST_DECISSION_KEY, ASK_AGAIN)
										.commit();
							}
						}).setNegativeButton(resDozeDoNotWhitelist, new OnClickListener() {
							@TargetApi(23)
							@Override
							public void onClick(DialogInterface dialog, int which) {
								sharedPreferences.edit()
										.putInt(WHITELIST_DECISSION_KEY, DO_NOT_WHITELIST).commit();
							}

						}).show();
				break;
			default:
				throw new IllegalStateException();
			}
		} else {
			requestWhitelist(context, myPackage);
		}
	}

	public static boolean forgetDecission(SharedPreferences sharedPreferences) {
		return sharedPreferences.edit().putInt(WHITELIST_DECISSION_KEY, ASK_AGAIN).commit();
	}

	@TargetApi(23)
	private static void requestWhitelist(Context context, String myPackage) {
		Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.scheme("package").opaquePart(myPackage);
		intent.setData(uriBuilder.build());
		context.startActivity(intent);
	}

	private static boolean isSupported() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
	}
}
