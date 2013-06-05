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

package org.projectmaxs.shared.activities;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public abstract class EditTextWatcher implements OnFocusChangeListener, TextView.OnEditorActionListener {
	protected final EditText mEditText;
	protected String mBeforeText;
	private boolean mInUse = false;

	public EditTextWatcher(EditText editText) {
		this.mEditText = editText;
		editText.setOnFocusChangeListener(this);
		editText.setOnEditorActionListener(this);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			inFocus(v);
		}
		else {
			maybeCallLostFocusOrDone(v);
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		switch (actionId) {
		case EditorInfo.IME_ACTION_DONE:
		case EditorInfo.IME_ACTION_GO:
		case EditorInfo.IME_ACTION_NEXT:
			maybeCallLostFocusOrDone(v);
			break;
		default:
			break;
		}

		return false;
	}

	public void inFocus(View v) {
		mInUse = true;
		mBeforeText = mEditText.getText().toString();
	}

	public abstract void lostFocusOrDone(View v);

	private void maybeCallLostFocusOrDone(View v) {
		if (mInUse) lostFocusOrDone(v);
		mInUse = false;
	}
}
