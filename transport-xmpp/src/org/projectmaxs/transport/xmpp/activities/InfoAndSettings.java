package org.projectmaxs.transport.xmpp.activities;

import java.util.Set;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.stringprep.XmppStringprepException;
import org.minidns.MiniDnsConfiguration;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.jul.JULHandler;
import org.projectmaxs.shared.global.util.ActivityUtil;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.global.util.SpannedUtil;
import org.projectmaxs.shared.transport.AndroidDozeUtil;
import org.projectmaxs.transport.xmpp.R;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.util.ConnectivityManagerUtil;
import org.projectmaxs.transport.xmpp.xmppservice.StateChangeListener;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPBundleAndDefer;
import org.projectmaxs.transport.xmpp.xmppservice.XMPPService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InfoAndSettings extends Activity {
	static {
		JULHandler.setAsDefaultUncaughtExceptionHandler();
	}

	private static final Log LOG = Log.getLog();

	private static final int NEW_MASTER_ADDRESS_REQUEST_CODE = 1;
	private static final int SAVE_XMPP_CREDENTIALS_REQUEST_CODE = NEW_MASTER_ADDRESS_REQUEST_CODE + 1;

	private Settings mSettings;
	private PingServerButtonHandler mPingServerButtonHandler;

	private LinearLayout mMasterAddresses;
	private TextView mJID;

	public void openAdvancedSettings(View view) {
		startActivity(new Intent(this, AdvancedSettings.class));
	}

	public void showAbout(View view) {
		SpannableStringBuilder sb = SpannedUtil.createdAboutDialog(this, "transport-xmpp",
				R.string.app_name, R.string.version, R.string.copyright, R.string.authors,
				R.string.gplv3);
		sb.append('\n');
		sb.append(Html.fromHtml(
// @formatter:off
"<h1>Open Source</h1>" +
"&#8226; <a href=\"http://www.igniterealtime.org/projects/smack\">Smack</a><br>" +
"&#8226; <a	href=\"https://github.com/ge0rg/MemorizingTrustManager\">MemorizingTrustManager</a><br>" +
"&#8226; <a	href=\"https://github.com/rtreffer/minidns\">MiniDNS</a><br>" +
"<h2>Smack (XMPP Client Library)</h2>" +
SmackConfiguration.getVersion() + "<br>" +
"<a href=\"http://www.igniterealtime.org/projects/smack\">http://www.igniterealtime.org/projects/smack</a><br>" +
"<br>" +
"Copyright © 2011-2018 Florian Schmaus<br>" +
"Copyright © 2013-2014 Georg Lukas<br>" +
"Copyright © 2014 Lars Noschinski<br>" +
"Copyright © 2014 Vyacheslav Blinov<br>" +
"Copyright © 2014 Andriy Tsykholyas<br>" +
"Copyright © 2009-2013 Robin Collier<br>" +
"Copyright © 2009 Jonas Ådahl<br>" +
"Copyright © 2003-2010 Jive Software<br>" +
"Copyright © 2001-2004 Apache Software Foundation<br>" +
"Apache License, Version 2.0<br>" +
"<h2>MemorizingTrustManager</h2>" +
"<a href=\"https://github.com/ge0rg/MemorizingTrustManager\">https://github.com/ge0rg/MemorizingTrustManager</a><br>" +
"<br>" +
"Copyright © 2010-2104 Georg Lukas<br>" +
"MIT License<br>" +
"<h2>MiniDNS</h2>" +
MiniDnsConfiguration.getVersion() + "<br>" +
"<a href=\"https://github.com/rtreffer/minidns\">https://github.com/rtreffer/minidns<a/><br>" +
"<br>" +
"Apache License, Version 2.0<br>" +
"<h1>License Links</h1>" +
"&#8226; <a href=\"http://www.apache.org/licenses/LICENSE-2.0\">Apache License, Version 2.0</a><br>" +
"&#8226; <a href=\"http://opensource.org/licenses/MIT\">MIT License</a>"
// @formatter:on
		));
		ActivityUtil.showSimpleTextView(this, sb, R.string.close);
	}

	public void registerAccount(View view) {
		final EntityBareJid jid = mSettings.getJid();
		final String password = mSettings.getPassword();
		if (jid == null) {
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
				if (!ConnectivityManagerUtil.hasDataConnection(InfoAndSettings.this)) {
					showToast("Data connection not available", Toast.LENGTH_SHORT);
					return;
				}

				try {
					final Localpart username = jid.getLocalpart();
					final AbstractXMPPConnection connection = new XMPPTCPConnection(
							mSettings.getConnectionConfiguration(InfoAndSettings.this));
					showToast("Connecting to server", Toast.LENGTH_SHORT);
					connection.connect();
					AccountManager accountManager = AccountManager.getInstance(connection);
					showToast("Connected, trying to create account", Toast.LENGTH_SHORT);
					accountManager.createAccount(username, password);
					connection.disconnect();
				} catch (Exception e) {
					LOG.i("registerAccount", e);
					showToast("Error creating account: " + e, Toast.LENGTH_LONG);
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

	public void onAddMasterAddressButtonClicked(View view) {
		startActivityForResult(new Intent(this, EnterMasterAddress.class), NEW_MASTER_ADDRESS_REQUEST_CODE);
	}

	public void onEditXmppCredentialsButtonClicked(View view) {
		startActivityForResult(new Intent(this, EnterXmppCredentials.class), SAVE_XMPP_CREDENTIALS_REQUEST_CODE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infoandsettings);

		mSettings = Settings.getInstance(this);

		// Views
		mMasterAddresses = (LinearLayout) findViewById(R.id.masterAddresses);
		mJID = (TextView) findViewById(R.id.jid);

		// initialize the master jid linear layout if there are already some
		// configured
		Set<EntityBareJid> masterJids = mSettings.getMasterJids();
		for (EntityBareJid masterAddress : masterJids) {
			MasterAddressView.createNewAndAddUnderLayout(this, mMasterAddresses, masterAddress);
		}

		if (mSettings.getJid() != null) mJID.setText(mSettings.getJid());

		mPingServerButtonHandler = new PingServerButtonHandler(this);

		AndroidDozeUtil.requestWhitelistIfNecessary(this, mSettings.getSharedPreferences(),
				R.string.DozeAskForWhitelist, R.string.DozeDoNotWhitelist, R.string.AskAgain,
				R.string.DozeWhitelist);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case NEW_MASTER_ADDRESS_REQUEST_CODE:
			handleNewMasterAddressResult(resultCode, data);
			break;
		case SAVE_XMPP_CREDENTIALS_REQUEST_CODE:
			handleSaveXmppCredentialsResult(resultCode, data);
			break;
		default:
			LOG.w("Unknown request code " + requestCode + " in onActivityResult, with result code "
					+ resultCode + " and intent " + data);
			break;
		}
	}

	private final void handleNewMasterAddressResult(int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			LOG.d("Non ok result code " + resultCode + " for NEW_MASTER_ADDRESS_REQUEST_CODE");
			return;
		}

		String newMasterAddressString = data.getStringExtra(GlobalConstants.EXTRA_ENTITY_BARE_JID);
		EntityBareJid newMasterAddress;
		try {
			newMasterAddress = JidCreate.entityBareFrom(newMasterAddressString);
		} catch (XmppStringprepException e) {
			throw new AssertionError(e);
		}
		mSettings.addMasterJid(newMasterAddress);
		MasterAddressView.createNewAndAddUnderLayout(this, mMasterAddresses, newMasterAddress);
		XMPPService xmppService = XMPPService.getInstance(this);
		xmppService.notifyAboutNewMasterAddress(newMasterAddress);
	}

	private final void handleSaveXmppCredentialsResult(int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			LOG.d("Non ok result code " + resultCode + " for SAVE_XMPP_CREDENTIALS_REQUEST_CODE");
			return;
		}

		// Just update the text, the EnterXmppCredentials activity already saved the credentials
		mJID.setText(mSettings.getJid());
	}

	class PingServerButtonHandler extends StateChangeListener implements OnClickListener {

		private final Button mPingServerButton;

		private volatile PingManager mPingManager;

		@SuppressLint("StaticFieldLeak")
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
		@SuppressLint("StaticFieldLeak")
		@Override
		public synchronized void onClick(View v) {
			Toast.makeText(InfoAndSettings.this, "Sending ping to server", Toast.LENGTH_SHORT)
					.show();
			new AsyncTask<PingManager, Void, Long>() {
				@Override
				protected Long doInBackground(PingManager... pingManagers) {
					XMPPBundleAndDefer.disableBundleAndDefer();
					try {
						long start = System.currentTimeMillis();
						boolean res = pingManagers[0].pingMyServer();
						if (res) {
							long stop = System.currentTimeMillis();
							return stop - start;
						}
					} catch (InterruptedException | SmackException e) {
						LOG.w("pingMyServer", e);
					} catch (RuntimeException e) {
						LOG.e("pingMyServer: RuntimeException", e);
					} finally {
						XMPPBundleAndDefer.enableBundleAndDefer();
					}
					return (long) -1;
				}

				@Override
				protected void onPostExecute(Long result) {
					String text;
					if (result > 0) {
						text = "Pong received within "
								+ SharedStringUtil.humanReadableMilliseconds(result)
								+ ". Ping successful. ☺";
					} else {
						text = "Pong timeout. Ping failed!";
					}
					Toast.makeText(InfoAndSettings.this, text, Toast.LENGTH_LONG).show();
				}
			}.execute(mPingManager);
		}

		@Override
		public synchronized void connected(XMPPConnection connection) {
			mPingManager = PingManager.getInstanceFor(connection);
			setPingButtonEnabled(true);
		}

		@Override
		public synchronized void disconnected(XMPPConnection connection) {
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
