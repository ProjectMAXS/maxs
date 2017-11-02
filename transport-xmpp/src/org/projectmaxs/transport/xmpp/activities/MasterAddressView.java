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
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.R;
import org.projectmaxs.transport.xmpp.Settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MasterAddressView extends RelativeLayout {

	private static final Log LOG = Log.getLog();

	private final Settings mSettings;

	private final LinearLayout mParentLayout;
	private final ImageButton mDeleteMasterAddress;
	private final EntityBareJid mMasterAddress;

	private final TextView mMasterAddressTextView;

	public static void createNewAndAddUnderLayout(Context context, LinearLayout parentLayout,
			EntityBareJid masterAddress) {
		MasterAddressView masterAddressView = new MasterAddressView(context, parentLayout,
				masterAddress);
		parentLayout.addView(masterAddressView);
	}

	private MasterAddressView(Context context, LinearLayout parentLayout, EntityBareJid masterAddress) {
		super(context);

		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.master_address, this);

		mSettings = Settings.getInstance(context);
		mParentLayout = parentLayout;
		mDeleteMasterAddress = (ImageButton) findViewById(R.id.deleteMasterAddress);
		mMasterAddress = masterAddress;

		mMasterAddressTextView = (TextView) findViewById(R.id.masterAddress);
		mMasterAddressTextView.setText(masterAddress);

		mDeleteMasterAddress.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				mParentLayout.removeView(MasterAddressView.this);
				boolean removed = mSettings.removeMasterJid(mMasterAddress);
				if (!removed) {
					LOG.w("Previous master JID " + mMasterAddress + " was not found in settings when removing it");
				}
			}
		});
	}

}
