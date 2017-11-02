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
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.transport.xmpp.R;
import org.projectmaxs.transport.xmpp.activities.EntityBareJidTextWatcher.OnInvalidJidCallback;
import org.projectmaxs.transport.xmpp.activities.EntityBareJidTextWatcher.OnValidBareJidCallback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

public class EnterMasterAddress extends Activity {

	private EditText mNewMasterAddress;
	private ImageButton mSaveNewMasterAddress;

	private EntityBareJid mLatestValidEntityBareJid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_master_address);

		mNewMasterAddress = (EditText) findViewById(R.id.newMasterAddress);
		mSaveNewMasterAddress = (ImageButton) findViewById(R.id.saveNewMasterAddress);

		mSaveNewMasterAddress.setEnabled(false);

		EntityBareJidTextWatcher.watch(mNewMasterAddress, new OnValidBareJidCallback() {
			@Override
			public void onValidBareJid(EntityBareJid entityBareJid, Editable editable) {
				mLatestValidEntityBareJid = entityBareJid;
				mSaveNewMasterAddress.setEnabled(true);
			}
		}, new OnInvalidJidCallback() {
			@Override
			public void onInvalidJid(Editable editable) {
				mSaveNewMasterAddress.setEnabled(false);
				mLatestValidEntityBareJid = null;
			}
		});
	}

	public void onSaveNewMasterAddressButtonClicked(View view) {
		Intent data = new Intent();
		data.putExtra(GlobalConstants.EXTRA_ENTITY_BARE_JID, mLatestValidEntityBareJid.toString());
		setResult(RESULT_OK, data);
		finish();
	}
}
