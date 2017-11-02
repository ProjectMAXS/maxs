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

import org.jxmpp.jid.EntityBareJid;
import org.projectmaxs.transport.xmpp.R;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.activities.EntityBareJidTextWatcher.OnInvalidJidCallback;
import org.projectmaxs.transport.xmpp.activities.EntityBareJidTextWatcher.OnValidBareJidCallback;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class EnterXmppCredentials extends Activity {

	private Settings mSettings;

	private EditText mMaxsXmppAddress;
	private EditText mMaxsXmppPassword;
	private ImageButton mSaveXmppCredentials;

	private EntityBareJid mLatestValidEntityBareJid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_xmpp_credentials);

		mSettings = Settings.getInstance(this);

		mMaxsXmppAddress = (EditText) findViewById(R.id.maxsXmppAddress);
		mMaxsXmppPassword = (EditText) findViewById(R.id.maxsXmppPassword);
		mSaveXmppCredentials = (ImageButton) findViewById(R.id.saveXmppCredentials);

		EntityBareJid currentMaxsAddress = mSettings.getJid();
		if (currentMaxsAddress != null) {
			mMaxsXmppAddress.setText(currentMaxsAddress);
		}
		String currentPassword = mSettings.getPassword();
		if (!currentPassword.isEmpty()) {
			mMaxsXmppPassword.setText(currentPassword);
		}

		// Make sure the button is in the right state. That is 'disabled' if something is missing,
		// enabled otherwise.
		setSaveXmppCredentialsButtonState();

		EntityBareJidTextWatcher.watch(mMaxsXmppAddress, new OnValidBareJidCallback() {
			@Override
			public void onValidBareJid(EntityBareJid entityBareJid, Editable editable) {
				mLatestValidEntityBareJid = entityBareJid;
				setSaveXmppCredentialsButtonState();
			}
		}, new OnInvalidJidCallback() {
			@Override
			public void onInvalidJid(Editable editable) {
				mLatestValidEntityBareJid = null;
				setSaveXmppCredentialsButtonState();
			}
		});

		mMaxsXmppPassword.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				setSaveXmppCredentialsButtonState();
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
	}

	public void onSaveXmppCredentialsButtonClicked(View view) {
		mSettings.setJidAndPassword(mLatestValidEntityBareJid, mMaxsXmppPassword.getEditableText());
		setResult(RESULT_OK, null);
		finish();
	}

	private void setSaveXmppCredentialsButtonState() {
		boolean buttonEnabled;
		if (mLatestValidEntityBareJid != null & !mMaxsXmppPassword.getText().toString().isEmpty()) {
			buttonEnabled = true;
		} else {
			buttonEnabled = false;
		}
		mSaveXmppCredentials.setEnabled(buttonEnabled);
	}
}
