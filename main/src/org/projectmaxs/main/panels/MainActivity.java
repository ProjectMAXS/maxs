package org.projectmaxs.main.panels;

import org.projectmaxs.main.MAXSService;
import org.projectmaxs.main.MAXSService.LocalService.LocalBinder;
import org.projectmaxs.main.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class MainActivity extends Activity {
	/** Called when the activity is first created. */

	MAXSService.LocalService mMAXSLocalService = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mMAXSLocalService == null) {
			Intent intent = new Intent(this, MAXSService.LocalService.class);
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
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mMAXSLocalService = null;
		}
	};

}
