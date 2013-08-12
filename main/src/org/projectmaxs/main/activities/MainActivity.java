package org.projectmaxs.main.activities;

import java.util.Collections;
import java.util.List;

import org.projectmaxs.main.MAXSService;
import org.projectmaxs.main.MAXSService.StartStopListener;
import org.projectmaxs.main.R;
import org.projectmaxs.main.Settings;
import org.projectmaxs.main.TransportRegistry;
import org.projectmaxs.main.TransportRegistry.ChangeListener;
import org.projectmaxs.main.util.Constants;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.maintransport.TransportInformation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final Log LOG = Log.getLog();

	private Settings mSettings;
	private StartStopListener mListener;

	private Button mStartStopButton;
	private ListView mTransportList;
	private TransportInformationAdapter mTIAdapter;

	private List<TransportInformation> mTransportInformationList;
	private TransportRegistry.ChangeListener mTransportRegistryListener = new TransportRegistry.ChangeListener() {
		@Override
		public void transportUnregistered(final TransportInformation transportInformation) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTransportInformationList.remove(transportInformation);
					sortAndNotify();
				}
			});
		}

		@Override
		public void transportRegistered(final TransportInformation transportInformation) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mTransportInformationList.add(transportInformation);
					sortAndNotify();
				}
			});
		}

		private void sortAndNotify() {
			Collections.sort(mTransportInformationList);
			mTIAdapter.notifyDataSetChanged();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mSettings = Settings.getInstance(this);

		// Views
		mStartStopButton = (Button) findViewById(R.id.buttonStartStop);
		mTransportList = (ListView) findViewById(R.id.transportsList);

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
			public void onServiceStart(final MAXSService service) {
				status(service.getString(R.string.stopService));
			}

			public void onServiceStop(final MAXSService service) {
				status(service.getString(R.string.startService));
			}
		};
		MAXSService.addStartStopListener(mListener);

		if (mSettings.connectOnMainScreen() && MAXSService.isRunning()) {
			LOG.d("connectOnMainScreen enabled and service not running, calling startService");
			startService(new Intent(Constants.ACTION_START_SERVICE));
		}

		// Race condition between getCopyAddListener and new
		// TransportInformationAdapter
		mTransportInformationList = TransportRegistry.getInstance(this).getCopyAddListener(mTransportRegistryListener);
		mTIAdapter = new TransportInformationAdapter(this, mTransportInformationList);
		mTransportList.setAdapter(mTIAdapter);

		// request all transports to update their status
		for (TransportInformation ti : mTransportInformationList) {
			Intent intent = new Intent(TransportConstants.ACTION_REQUEST_TRANSPORT_STATUS);
			intent.setClassName(ti.getTransportPackage(), ti.getTransportPackage()
					+ TransportConstants.TRANSPORT_SERVICE);
			startService(intent);
		}
	}

	@Override
	public void onDestroy() {
		MAXSService.removeStartStopListener(mListener);
		TransportRegistry.getInstance(this).removeChangeListener(mTransportRegistryListener);
	}

	private void status(final String startStopButtonText) {
		MainActivity.this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mStartStopButton.setText(startStopButtonText);
			}
		});
	}

	class TransportInformationAdapter extends ArrayAdapter<TransportInformation> {
		final List<TransportInformation> mData;
		final Context mContext;

		public TransportInformationAdapter(Context context, List<TransportInformation> data) {
			super(context, R.layout.transports_listview_row, data);
			mData = data;
			mContext = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (mData.size() <= position) return null;

			final TransportInformation ti = mData.get(position);
			final String transportName = ti.getTransportName();
			final String transportPackage = ti.getTransportPackage();
			final String transportStatus = TransportRegistry.getInstance(mContext).getStatus(transportPackage);
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = MainActivity.this.getLayoutInflater();
				row = inflater.inflate(R.layout.transports_listview_row, parent, false);

			}
			else {
				ChangeListener cl = (ChangeListener) row.getTag();
				TransportRegistry.getInstance(mContext).removeChangeListener(cl);
			}

			final TextView textTransportName = (TextView) row.findViewById(R.id.textTransportName);
			final TextView textTransportPackage = (TextView) row.findViewById(R.id.textTransportPackage);
			final TextView textTransportStatus = (TextView) row.findViewById(R.id.textTransportStatus);
			final Button more = (Button) row.findViewById(R.id.buttonTransportMore);

			ChangeListener cl = new ChangeListener() {
				@Override
				public void transportStatusChanged(String changedTransportPackage, String status) {
					if (transportPackage.equals(changedTransportPackage)) setText(textTransportStatus, status);
				};
			};
			row.setTag(cl);
			TransportRegistry.getInstance(mContext).addChangeListener(cl);

			final Intent intent = new Intent();
			intent.setClassName(transportPackage, transportPackage + ".activities.InfoAndSettings");
			more.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(intent);
				}
			});
			textTransportName.setText(transportName);
			textTransportPackage.setText(transportPackage);
			textTransportStatus.setText(transportStatus);

			return row;
		}
	}

	private void setText(final TextView textView, final String text) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView.setText(text);
			}
		});
	}

	public void openAdvancedSettings(View view) {
		startActivity(new Intent(this, AdvancedSettings.class));
	}

	public void openModules(View view) {
		startActivity(new Intent(this, Modules.class));
	}

	public void openImportExportSettings(View view) {
		startActivity(new Intent(this, ImportExportSettings.class));
	}

	public void discoverComponents(View view) {
		Intent intent = new Intent(GlobalConstants.ACTION_REGISTER);
		sendBroadcast(intent);
	}
}
