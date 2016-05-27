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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class PermissionUtil {

	private static final Log LOG = Log.getLog();

	private static final AtomicInteger NEXT_REQUEST_CODE = new AtomicInteger();

	private static final Map<Integer, PendingRequestData> PENDING_REQUESTS = new ConcurrentHashMap<>();

	private static class PendingRequestData {
		PendingRequestData(String[] permissionsToRequest, Intent postServiceIntent) {
			this.permissionsToRequest = permissionsToRequest;
			this.postServiceIntent = postServiceIntent;
		}

		public final String[] permissionsToRequest;
		public final Intent postServiceIntent;
	}

	/**
	 * Check if the requested permissions have been granted. Request them if not. This does nothing
	 * if the Android version is lower than 6.0.
	 *
	 * @param context
	 *            a Context.
	 * @param postServiceIntent
	 *            a optional service intent, fired after the user granted all permissions.
	 * @return <code>true</code> if the permissions are ok, <code>false</code>otherwise.
	 */
	@TargetApi(23)
	public static boolean checkAndRequestIfNecessary(Context context, Intent postServiceIntent) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return true;
		}

		PackageManager packageManager = context.getPackageManager();

		PackageInfo packageInfo;
		try {
			packageInfo = packageManager.getPackageInfo(context.getPackageName(),
					PackageManager.GET_PERMISSIONS);
		} catch (NameNotFoundException e) {
			throw new AssertionError(e);
		}

		List<String> permissionsToRequest = new LinkedList<>();
		for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
			String permission = packageInfo.requestedPermissions[i];
			int res = context.checkSelfPermission(permission);
			switch (res) {
			case PackageManager.PERMISSION_DENIED:
				break;
			case PackageManager.PERMISSION_GRANTED:
			default:
				continue;
			}

			PermissionInfo permissionInfo;
			try {
				permissionInfo = packageManager.getPermissionInfo(permission,
						PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
				throw new AssertionError(e);
			}
			if (permissionInfo.protectionLevel != PermissionInfo.PROTECTION_DANGEROUS) {
				continue;
			}

			permissionsToRequest.add(permission);
		}

		if (permissionsToRequest.isEmpty()) {
			return true;
		}

		LOG.i("Going to request the following permissions: "
				+ SharedStringUtil.listCollection(permissionsToRequest) + '.');

		String[] permissionToRequestArray = permissionsToRequest
				.toArray(new String[permissionsToRequest.size()]);
		int request = NEXT_REQUEST_CODE.incrementAndGet();
		PENDING_REQUESTS.put(request, new PendingRequestData(permissionToRequestArray, postServiceIntent));

		Intent intent = new Intent(context, RequestPermissionDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		return false;
	}

	/**
	 * A invisible activity. It will only show an {@link AlertDialog} explaining the user what this
	 * MAXS components needs some permissions and that the user should grant them.
	 */
	@TargetApi(23)
	public static class RequestPermissionDialog extends Activity {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

			TextView textView = new TextView(this);
			textView.setText(RTool.getStringId(this, "request_permission_info"));
			dialogBuilder.setView(textView);

			dialogBuilder.setPositiveButton(RTool.getStringId(this, "proceed"),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Iterator<Entry<Integer, PendingRequestData>> it = PENDING_REQUESTS
									.entrySet()
									.iterator();
							while (it.hasNext()) {
								Entry<Integer, PendingRequestData> entry = it.next();
								requestPermissions(entry.getValue().permissionsToRequest,
										entry.getKey());
							}
						}
					});

			dialogBuilder.create().show();
		}

		@Override
		public void onRequestPermissionsResult(int requestCode, String[] permissions,
				int[] grantResults) {

			final PendingRequestData pendingRequestData = PENDING_REQUESTS.remove(requestCode);
			if (pendingRequestData == null) {
				LOG.w("Could not find request for " + requestCode);
				return;
			}

			if (permissions.length == 0) {
				LOG.d("User cancelled permission dialog");
				return;
			}

			List<String> deniedPermissions = new ArrayList<>(permissions.length);
			for (int i = 0; i < permissions.length; i++) {
				if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
					LOG.i("User granted " + permissions[i] + ". \\o/");
					continue;
				}
				LOG.w("User did not grant " + permissions[i] + ". :(");
				deniedPermissions.add(permissions[i]);
			}

			if (!deniedPermissions.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				sb.append(RTool.getString(this, "request_permission_denied"));
				sb.append(SharedStringUtil.listCollection(deniedPermissions));
				sb.append('.');
				Toast.makeText(this, sb, Toast.LENGTH_LONG).show();
			} else if (pendingRequestData.postServiceIntent != null) {
				LOG.i("User granted *all* permissions. PostServiceIntent set, starting "
						+ pendingRequestData.postServiceIntent);
				this.startService(pendingRequestData.postServiceIntent);
			}

			// We are done here
			finish();
		}
	}
}
