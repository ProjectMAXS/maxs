package org.projectmaxs.main.panels;

import org.jivesoftware.smack.Connection;
import org.projectmaxs.main.MAXSService;
import org.projectmaxs.main.MAXSService.LocalBinder;
import org.projectmaxs.main.R;
import org.projectmaxs.main.StateChangeListener;
import org.projectmaxs.main.XMPPService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */

	private boolean serviceWasNotConnectedBefore = true;
	private MAXSService mMAXSLocalService = null;
	private Button mConnButton;
	private TextView mStatusText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Buttons
		mConnButton = (Button) findViewById(R.id.connButton);
		mStatusText = (TextView) findViewById(R.id.statusText);

		mConnButton.setEnabled(false);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mMAXSLocalService == null) {
			Intent intent = new Intent(this, MAXSService.class);
			bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mMAXSLocalService != null) {
			unbindService(mConnection);
			mMAXSLocalService = null;
		}
	}

	ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mMAXSLocalService = binder.getService();

			if (serviceWasNotConnectedBefore) {
				mMAXSLocalService.getXMPPService().addListener(new StateChangeListener() {
					@Override
					public void connected(Connection con) {
						mStatusText.setText("connected");
					}

					@Override
					public void disconnected(Connection con) {
						mStatusText.setText("disconnected");
					}

					@Override
					public void connecting() {
						mStatusText.setText("connecting");
					}

					@Override
					public void disconnecting() {
						mStatusText.setText("disconnecting");
					}
				});
				serviceWasNotConnectedBefore = false;
			}

			mConnButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mMAXSLocalService != null) {
						XMPPService.State state = mMAXSLocalService.getXMPPService().getCurrentState();
						switch (state) {
						case Connected:
							mMAXSLocalService.stopService();
							break;
						case Disconnected:
							mMAXSLocalService.startService();
							break;
						}
					}
				}

			});
			mConnButton.setEnabled(true);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mMAXSLocalService = null;
			mConnButton.setEnabled(false);
		}
	};

}
