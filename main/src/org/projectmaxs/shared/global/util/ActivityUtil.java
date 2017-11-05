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

import android.app.Activity;
import android.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class ActivityUtil {

	public static void showSimpleTextView(Activity activity, CharSequence cs, int closeButtonText) {
		final TextView textView = new TextView(activity);
		textView.setText(cs);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		// Sadly we can't make this text view also selectable
		// http://stackoverflow.com/questions/14862750
		// @formatter:off
		final AlertDialog alertDialog = new AlertDialog.Builder(activity)
			.setPositiveButton(activity.getResources().getString(closeButtonText), null)
			.setView(textView)
			.create();
		// @formatter:on
		alertDialog.show();
	}
}
