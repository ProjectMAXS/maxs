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

package org.projectmaxs.module.nfc.activities;

import org.projectmaxs.module.nfc.NfcDiscoveredService;
import org.projectmaxs.module.nfc.Settings;
import org.projectmaxs.shared.global.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class NfcDiscoveredActivity extends Activity {
	private static final Log LOG = Log.getLog();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LOG.initialize(Settings.getInstance(this));
		// The getIntent should usually be done in onResume(), but since we call finish() as last of
		// this method, this activity will never get resumed, it will be always re-created.
		Intent causingIntent = getIntent();
		LOG.d("onCreated started with intent: " + causingIntent);
		Intent intent = new Intent(this, NfcDiscoveredService.class);
		intent.putExtra(NfcDiscoveredService.CAUSING_INTENT_EXTRA, causingIntent);
		startService(intent);
		finish();
	}
}
