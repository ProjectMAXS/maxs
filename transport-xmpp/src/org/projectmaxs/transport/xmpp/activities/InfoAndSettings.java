package org.projectmaxs.transport.xmpp.activities;

import java.util.Iterator;
import java.util.Set;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.ping.PingManager;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.SpannedUtil;
import org.projectmaxs.transport.xmpp.R;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.util.ConnectivityManagerUtil;
import org.projectmaxs.transport.xmpp.util.XMPPUtil;
import org.projectmaxs.transport.xmpp.xmppservice.StateChangeListener;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InfoAndSettings extends Activity {

	private static final Log LOG = Log.getLog();

	private Settings mSettings;
	private PingServerButtonHandler mPingServerButtonHandler;

	private LinearLayout mMasterAddresses;
	private EditText mFirstMasterAddress;
	private EditText mJID;
	private String mLastJidText;
	private EditText mPassword;
	private Button mAdvancedSettings;

	public void openAdvancedSettings(View view) {
		startActivity(new Intent(this, AdvancedSettings.class));
	}

	public void showAbout(View view) {
		final SpannableStringBuilder sb = new SpannableStringBuilder();
		final String appName = getResources().getString(R.string.app_name);
		sb.append(Html.fromHtml("<h1>" + appName + "</h1>"));
		sb.append(getResources().getString(R.string.version)).append('\n');
		sb.append(getResources().getString(R.string.copyright))
				.append(" (")
				.append(SpannedUtil.createAuthorsLink("transport-xmpp",
						getResources().getString(R.string.authors))).append(")\n");
		sb.append('\n');
		sb.append(appName).append(' ').append(getResources().getText(R.string.gplv3)).append('\n');
		sb.append('\n');
		sb.append(Html.fromHtml(
// @formatter:off
"<h1>Open Source</h1>" +
"&#8226; <a href=\"http://asmack.org\">aSmack</a><br>" +
"&#8226; <a	href=\"https://github.com/ge0rg/MemorizingTrustManager\">MemorizingTrustManager</a><br>" +
"<h2>aSmack</h2>" +
"<a href=\"http://asmack.org\">http://asmack.org</a><br>" +
"<br>" +
"&#8226; Smack (XMPP Client Library)<br>" +
"Copyright © 2003-2010 Jive Software<br>" +
"Copyright © 2001-2004 Apache Software Foundation<br>" +
"Copyright © 2011-2013 Florian Schmaus<br>" +
"Copyright © 2013 Georg Lukas<br>" +
"Copyright © 2013 Robin Collier<br>" +
"Copyright © 2009 Jonas Ådahl<br>" +
"Apache License, Version 2.0<br>" +
"&#8226; Apache Harmony (SASL/XML)<br>" +
"Copyright © 2006, 2010 Apache Software Foundation<br>" +
"Apache License, Version 2.0<br>" +
"&#8226; novell-openldap-jldap (SASL)<br>" +
"Copyright © 2002-2003 Novell, Inc.<br>" +
"OpenLDAP Public License, Version 2.8<br>" +
"&#8226; Apache qpid (SASL)<br>" +
"Copyright © 2006-2008 Apache Software Foundation<br>" +
"Apache License, Version 2.0<br>" +
"&#8226; jbosh (BOSH)<br>" +
"Copyright © 2009 Guenther Niess<br>" +
"Copyright © 2009 Mike Cumings<br>" +
"Copyright © 2001-2003 Apache Software Foundation<br>" +
"Apache License, Version 2.0<br>" +
"&#8226; dnsjava (DNS SRV)<br>" +
"Copyright © 1998-2011 Brian Wellington<br>" +
"BSD 2-Clause License<br>" +
"&#8226; aSmack custom code (various glue stuff)<br>" +
"Copyright © 2011-2013 Florian Schmaus<br>" +
"Copyright © 2009-2010 Rene Treffer<br>" +
"Apache License, Version 2.0<br>" +
"<h1>MemorizingTrustManager</h1>" +
"<a href=\"https://github.com/ge0rg/MemorizingTrustManager\">https://github.com/ge0rg/MemorizingTrustManager</a><br>" +
"<br>" +
"Copyright © 2010 Georg Lukas<br>" +
"MIT License<br>" +
"<h1>License Links</h1>" +
"&#8226; <a href=\"http://www.apache.org/licenses/LICENSE-2.0\">Apache License, Version 2.0</a><br>" +
"&#8226; <a href=\"http://opensource.org/licenses/MIT\">MIT License</a><br>" +
"&#8226; <a href=\"http://opensource.org/licenses/BSD-2-Clause\">BSD 2-Clause License</a><br>" +
"&#8226; <a	href=\"http://www.openldap.org/devel/gitweb.cgi?p=openldap-jldap.git;a=blob_plain;f=LICENSE;h=05ad7571e448b9d83ead5d4691274d9484574714;hb=HEAD\">OpenLDAP Public License, Version 2.8</a>"
// @formatter:on
				));
		final TextView textView = new TextView(this);
		textView.setText(sb);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
		// Sadly we can't make this text view also selectable
		// http://stackoverflow.com/questions/14862750
		// @formatter:off
		final AlertDialog alertDialog = new AlertDialog.Builder(this)
			.setPositiveButton(getResources().getString(R.string.close), null)
			.setView(textView)
			.create();
		// @formatter:on
		alertDialog.show();
	}

	public void registerAccount(View view) {
		final String jid = mSettings.getJid();
		final String password = mSettings.getPassword();
		if (jid.isEmpty()) {
			Toast.makeText(this, "Please enter a valid bare JID", Toast.LENGTH_SHORT).show();
			return;
		}
		if (password.isEmpty()) {
			Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
			return;
		}
		(new Thread() {

			@Override
			public void run() {
				SmackAndroid.init(InfoAndSettings.this);
				if (!ConnectivityManagerUtil.hasDataConnection(InfoAndSettings.this)) {
					showToast("Data connection not available", Toast.LENGTH_SHORT);
					return;
				}

				try {
					final String username = StringUtils.parseName(mSettings.getJid());
					final String password = mSettings.getPassword();
					final Connection connection = new XMPPConnection(
							mSettings.getConnectionConfiguration(InfoAndSettings.this));
					showToast("Connecting to server", Toast.LENGTH_SHORT);
					connection.connect();
					AccountManager accountManager = new AccountManager(connection);
					showToast("Connected, trying to create account", Toast.LENGTH_SHORT);
					accountManager.createAccount(username, password);
					connection.disconnect();
				} catch (XMPPException e) {
					LOG.i("registerAccount", e);
					showToast("Error creating account: " + e.getLocalizedMessage(),
							Toast.LENGTH_LONG);
					return;
				}
				showToast("Account created", Toast.LENGTH_SHORT);
			}

			private final void showToast(final String text, final int duration) {
				InfoAndSettings.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(InfoAndSettings.this, text, duration).show();
					}
				});
			}

		}).start();
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
					Toast.makeText(InfoAndSettings.this, "This is not a valid bare JID",
							Toast.LENGTH_LONG).show();
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

		mPingServerButtonHandler = new PingServerButtonHandler(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Removing this handler yields actually a problem: If onDestroy() is called shortly after
		// onCreate() and the XMPPService was not yet initialized/constructed, then this call may
		// lead to network IO and an NetworkOnMainThreadException. But a fix wouldn't be trivial
		// and this is a corner case.
		XMPPService.getInstance(this).removeListener(mPingServerButtonHandler);
	}

	private final EditText addEmptyMasterJidEditText() {
		EditText newEditText = new EditText(this);
		newEditText.setHint(getString(R.string.hint_jid));
		newEditText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
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
				Toast.makeText(InfoAndSettings.this, "This is not a valid bare JID",
						Toast.LENGTH_LONG).show();
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

	class PingServerButtonHandler extends StateChangeListener implements OnClickListener {

		private final Button mPingServerButton;

		private volatile PingManager mPingManager;

		public PingServerButtonHandler(Activity activity) {
			mPingServerButton = (Button) activity.findViewById(R.id.pingServer);
			mPingServerButton.setOnClickListener(this);

			// Ugly workaround for NetworkOnMainThreadException, because XMPPService's constructor
			// call leads to a call to Socks5Proxy.getSocks5Proxy, which does
			// InetAddress.getLocalHost().getHostAddress() which finally leads to some network IO.
			new AsyncTask<Activity, Void, XMPPService>() {
				@Override
				protected XMPPService doInBackground(Activity... activities) {
					return XMPPService.getInstance(activities[0]);
				}

				@Override
				protected void onPostExecute(XMPPService xmppService) {
					if (xmppService.isConnected()) {
						PingServerButtonHandler.this.mPingManager = PingManager
								.getInstanceFor(xmppService.getConnection());
						mPingServerButton.setEnabled(true);
					}
					xmppService.addListener(PingServerButtonHandler.this);
				}
			}.execute(activity);
		}

		/**
		 * This onClick() method can only be called when we are connected, because otherwise the
		 * button will be disabled. Therefore there is no need to check mPingManager for null.
		 */
		@Override
		public synchronized void onClick(View v) {
			Toast.makeText(InfoAndSettings.this, "Sending ping to server", Toast.LENGTH_SHORT)
					.show();
			new AsyncTask<PingManager, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(PingManager... pingManagers) {
					return pingManagers[0].pingMyServer();
				}

				@Override
				protected void onPostExecute(Boolean result) {
					String text;
					if (result) {
						text = "Pong received. Ping successful";
					} else {
						text = "Pong timeout. Ping failed!";
					}
					Toast.makeText(InfoAndSettings.this, text, Toast.LENGTH_LONG).show();
				}
			}.execute(mPingManager);
		}

		@Override
		public synchronized void connected(Connection connection) {
			mPingManager = PingManager.getInstanceFor(connection);
			setPingButtonEnabled(true);
		}

		@Override
		public synchronized void disconnected(Connection connection) {
			mPingManager = null;
			setPingButtonEnabled(false);
		}

		@Override
		public synchronized void disconnected(String reason) {
			mPingManager = null;
			setPingButtonEnabled(false);
		}

		private final void setPingButtonEnabled(final boolean enabled) {
			InfoAndSettings.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mPingServerButton.setEnabled(enabled);
				}
			});
		}
	}
}
