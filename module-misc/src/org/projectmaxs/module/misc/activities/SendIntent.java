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

package org.projectmaxs.module.misc.activities;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.module.MainUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Receiver for an action.SEND intent. N.B. that this can not be an Service and must be an Activity.
 * See also http://stackoverflow.com/a/11085642/194894
 * 
 */
public class SendIntent extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
		MainUtil.send(new Message(sharedText), this);
		finish();
	}

}
