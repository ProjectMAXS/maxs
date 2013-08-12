package org.projectmaxs.transport.xmpp.activities;

import java.util.Iterator;
import java.util.Set;

import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.R;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.util.XMPPUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class InfoAndSettings extends Activity {

	private static final Log LOG = Log.getLog();

	private Settings mSettings;

	private LinearLayout mMasterAddresses;
	private EditText mFirstMasterAddress;
	private EditText mJID;
	private String mLastJidText;
	private EditText mPassword;
	private Button mAdvancedSettings;

	public void openAdvancedSettings(View view) {
		startActivity(new Intent(this, AdvancedSettings.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infoandsettings);

		mSettings = Settings.getInstance(this);

		// Views
		mMasterAddresses = (LinearLayout) findViewById(R.id.masterAddresses);
		mFirstMasterAddress = (EditText) findViewById(R.id.firstMasterAddress);
		mJID = (EditText) findViewById(R.id.jid);
		mPassword = (EditText) findViewById(R.id.password);
		mAdvancedSettings = (Button) findViewById(R.id.advancedSettings);

		// Avoid the virtual keyboard by focusing a button
		mAdvancedSettings.requestFocus();

		new MasterAddressCallbacks(mFirstMasterAddress);
		new EditTextWatcher(mJID) {
			@Override
			public void lostFocusOrDone(View v) {
				String text = mJID.getText().toString();
				if (!XMPPUtil.isValidBareJid(text)) {
					Toast.makeText(InfoAndSettings.this, "This is not a valid bare JID", Toast.LENGTH_LONG).show();
					mJID.setText(mLastJidText);
					return;
				}
				mSettings.setJid(text);
			}
		};
		new EditTextWatcher(mPassword) {
			@Override
			public void lostFocusOrDone(View v) {
				mSettings.setPassword(mPassword.getText().toString());
			}
		};

		// initialize the master jid linear layout if there are already some
		// configured
		Set<String> masterJids = mSettings.getMasterJids();
		if (!masterJids.isEmpty()) {
			Iterator<String> it = masterJids.iterator();
			mFirstMasterAddress.setText(it.next());
			while (it.hasNext()) {
				EditText et = addEmptyMasterJidEditText();
				et.setText(it.next());
			}
			addEmptyMasterJidEditText();
		}
		if (!mSettings.getJid().equals("")) mJID.setText(mSettings.getJid());
		if (!mSettings.getPassword().equals("")) mPassword.setText(mSettings.getPassword());

	}

	private final EditText addEmptyMasterJidEditText() {
		EditText newEditText = new EditText(this);
		newEditText.setHint(getString(R.string.hint_jid));
		new MasterAddressCallbacks(newEditText);
		mMasterAddresses.addView(newEditText);
		return newEditText;
	}

	private final class MasterAddressCallbacks extends EditTextWatcher {

		MasterAddressCallbacks(EditText editText) {
			super(editText);
		}

		public void lostFocusOrDone(View v) {
			String text = mEditText.getText().toString();
			if (text.equals("") && !mBeforeText.equals("")) {
				int childCount = mMasterAddresses.getChildCount();
				mSettings.removeMasterJid(mBeforeText);
				mMasterAddresses.removeView(mEditText);
				if (childCount <= 2) {
					mMasterAddresses.addView(mEditText, 2);
					mEditText.setHint(InfoAndSettings.this.getString(R.string.hint_jid));
				}
				return;
			}

			if (text.equals("")) return;

			// an attempt to change an empty master jid to an invalid jid. abort
			// here and leave the original value untouched
			if (!XMPPUtil.isValidBareJid(text)) {
				Toast.makeText(InfoAndSettings.this, "This is not a valid bare JID", Toast.LENGTH_LONG).show();
				mEditText.setText(mBeforeText);
			}
			// an empty master jid was change to a valid jid
			else if (mBeforeText.equals("")) {
				mSettings.addMasterJid(text);
				addEmptyMasterJidEditText();
			}
			// a valid master jid was changed with another valid value
			else if (!mBeforeText.equals(text)) {
				mSettings.removeMasterJid(mBeforeText);
				mSettings.addMasterJid(text);
			}
			return;
		}

	}
}
