package org.projectmaxs.main.activities;

import org.projectmaxs.main.MAXSService;
import org.projectmaxs.main.MAXSService.StartStopListener;
import org.projectmaxs.main.R;
import org.projectmaxs.main.Settings;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.shared.global.util.Log;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	private static final Log LOG = Log.getLog();

	private Settings mSettings;
	private StartStopListener mListener;

	private Button mStartStopButton;

	public void openAdvancedSettings(View view) {
		startActivity(new Intent(this, AdvancedSettings.class));
	}

	public void openModules(View view) {
		startActivity(new Intent(this, Modules.class));
	}

	public void openImportExportSettings(View view) {
		startActivity(new Intent(this, ImportExportSettings.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSettings = Settings.getInstance(this);

		// Views
		mStartStopButton = (Button) findViewById(R.id.buttonConnect);

		mStartStopButton.requestFocus();
		mStartStopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent;
				if (MAXSService.isRunning()) {
					intent = new Intent(Constants.ACTION_STOP_SERVICE);
				}
				else {
					intent = new Intent(Constants.ACTION_START_SERVICE);
				}
				MainActivity.this.startService(intent);
			}
		});
		mListener = new StartStopListener() {
			@Override
			public void onServiceStart(MAXSService service) {
			}

			public void onServiceStop(MAXSService service) {
			}
		};
		MAXSService.addStartStopListener(mListener);

		if (mSettings.connectOnMainScreen() && MAXSService.isRunning()) {
			LOG.d("connectOnMainScreen enabled and service not running, calling startService");
			startService(new Intent(Constants.ACTION_START_SERVICE));
		}
	}

	private void status(final String startStopButtonText) {
		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mStartStopButton.setText(startStopButtonText);
			}
		});
	}
}
