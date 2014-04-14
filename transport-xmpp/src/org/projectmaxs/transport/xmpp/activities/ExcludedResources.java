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

package org.projectmaxs.transport.xmpp.activities;

import java.util.Iterator;
import java.util.Set;

import org.projectmaxs.transport.xmpp.R;
import org.projectmaxs.transport.xmpp.Settings;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ExcludedResources extends Activity {

	private Settings mSettings;

	private LinearLayout mExcludedResources;
	private EditText mFirstExcludedresource;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.excludedresources);

		mSettings = Settings.getInstance(this);

		mExcludedResources = (LinearLayout) findViewById(R.id.excludedResources);
		mFirstExcludedresource = (EditText) findViewById(R.id.firstExcludedResource);

		new ExcludedResourceCallback(mFirstExcludedresource);

		// Initialize the excluded resources linear layout if there are already some configured
		Set<String> excludedResources = mSettings.getExcludedResources();
		if (!excludedResources.isEmpty()) {
			Iterator<String> it = excludedResources.iterator();
			mFirstExcludedresource.setText(it.next());
			while (it.hasNext()) {
				EditText et = addEmptyExcludeResourceEditText();
				et.setText(it.next());
			}
			addEmptyExcludeResourceEditText();
		}
	}

	private final EditText addEmptyExcludeResourceEditText() {
		EditText newEditText = new EditText(this);
		newEditText.setHint(getString(R.string.hint_resource));
		newEditText.setInputType(InputType.TYPE_CLASS_TEXT);
		new ExcludedResourceCallback(newEditText);
		mExcludedResources.addView(newEditText);
		return newEditText;
	}

	private final class ExcludedResourceCallback extends EditTextWatcher {

		public ExcludedResourceCallback(EditText editText) {
			super(editText);
		}

		@Override
		public void lostFocusOrDone(View v) {
			String text = mEditText.getText().toString();
			if (text.isEmpty() && !mBeforeText.isEmpty()) {
				int childCount = mExcludedResources.getChildCount();
				mSettings.removeMasterJid(mBeforeText);
				mExcludedResources.removeView(mEditText);
				if (childCount <= 2) {
					mExcludedResources.addView(mEditText, 2);
					mEditText.setHint(ExcludedResources.this.getString(R.string.hint_resource));
				}
				return;
			}

			if (text.isEmpty()) return;

			if (mBeforeText.isEmpty()) {
				mSettings.addExcludedResource(text);
				addEmptyExcludeResourceEditText();
			} else if (!mBeforeText.equals(text)) {
				mSettings.removeExcludedResource(mBeforeText);
				mSettings.addExcludedResource(text);
			}
		}
	}
}
