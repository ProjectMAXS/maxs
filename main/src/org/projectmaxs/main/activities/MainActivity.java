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
import org.projectmaxs.shared.global.util.DialogUtil;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.PackageManagerUtil;
import org.projectmaxs.shared.global.util.SpannedUtil;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.maintransport.TransportInformation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
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

		mStartStopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!TransportRegistry.getInstance(MainActivity.this)
						.isAtLeastOneTransportInstalled()) {
					DialogUtil
							.displayPackageInstallDialog(
									"In order to use MAXS you need to have at least one transport installed. "
											+ "We recommend the XMPP transport, which you can now install if you want.",
									GlobalConstants.TRANSPORT_PACKAGE, MainActivity.this);
					return;
				}

				Intent intent;
				if (MAXSService.isRunning()) {
					intent = new Intent(Constants.ACTION_STOP_SERVICE);
				} else {
					intent = new Intent(Constants.ACTION_START_SERVICE);
				}
				startService(intent);
			}
		});
		mListener = new StartStopListener() {
			@Override
			public void onServiceStart(final MAXSService service) {
				serviceRunning();
			}

			public void onServiceStop(final MAXSService service) {
				serviceNotRunning();
			}
		};
		MAXSService.addStartStopListener(mListener);
		if (MAXSService.isRunning()) {
			serviceRunning();
		} else {
			serviceNotRunning();
		}

		if (mSettings.connectOnMainScreen() && MAXSService.isRunning()) {
			LOG.d("connectOnMainScreen enabled and service not running, calling startService");
			startService(new Intent(Constants.ACTION_START_SERVICE));
		}

		// Race condition between getCopyAddListener and new
		// TransportInformationAdapter
		mTransportInformationList = TransportRegistry.getInstance(this).getCopyAddListener(
				mTransportRegistryListener);
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
		super.onDestroy();
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
			final String transportStatus = TransportRegistry.getInstance(mContext).getStatus(
					transportPackage);
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = MainActivity.this.getLayoutInflater();
				row = inflater.inflate(R.layout.transports_listview_row, parent, false);

			} else {
				ChangeListener cl = (ChangeListener) row.getTag();
				TransportRegistry.getInstance(mContext).removeChangeListener(cl);
			}

			final TextView textTransportName = (TextView) row.findViewById(R.id.textTransportName);
			final TextView textTransportPackage = (TextView) row
					.findViewById(R.id.textTransportPackage);
			final TextView textTransportStatus = (TextView) row
					.findViewById(R.id.textTransportStatus);
			final Button more = (Button) row.findViewById(R.id.buttonTransportMore);

			ChangeListener cl = new ChangeListener() {
				@Override
				public void transportStatusChanged(String changedTransportPackage, String status) {
					if (transportPackage.equals(changedTransportPackage))
						setText(textTransportStatus, status);
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

	/**
	 * Take actions for MainActivity if the service is running. This includes:
	 * - Set the text of the start/stop button to 'stop'
	 */
	private void serviceRunning() {
		status(getString(R.string.stopService));
	}

	/**
	 * Take actions for MainActivity if the service is not running. This includes:
	 * - Set the text of the start/stop button to 'start'
	 */
	private void serviceNotRunning() {
		status(getString(R.string.startService));
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

	public void showAbout(View view) {
		final SpannableStringBuilder sb = new SpannableStringBuilder();
		final String appName = getResources().getString(R.string.app_name);
		sb.append(Html.fromHtml("<h1>" + appName + "</h1>"));
		sb.append(getResources().getString(R.string.version)).append('\n');
		sb.append(getResources().getString(R.string.copyright))
				.append(" (")
				.append(SpannedUtil.createAuthorsLink("main",
						getResources().getString(R.string.authors))).append(")\n");
		sb.append('\n');
		sb.append(getResources().getText(R.string.info)).append('\n');
		sb.append('\n');
		sb.append(appName).append(' ').append(getResources().getText(R.string.gplv3));
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

	public void donate(View view) {
		final Intent BTC_INTENT = new Intent(Intent.ACTION_VIEW,
				Uri.parse("bitcoin:1AUuXzvVUh1HMb2kVYnDWz8TgjbJMaZqDt"));
		final Intent DONATE_INTENT = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://projectmaxs.org/homepage/index.html#Donate"));

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you want to donate directly with help of an Android Bitcoin Client, or do you want to view the \"Donate\" section on projectmaxs.org in a browser?");
		builder.setPositiveButton("Bitcoin", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (PackageManagerUtil.getInstance(MainActivity.this).isIntentAvailable(BTC_INTENT)) {
					startActivity(BTC_INTENT);
				} else {
					DialogUtil
							.displayPackageInstallDialog(
									"No Bitcoin Client found. Please consider installing \"Bitcoin Wallet\"",
									"de.schildbach.wallet", MainActivity.this);
				}
			}
		});
		builder.setNeutralButton("View \"Donate\" section", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				startActivity(DONATE_INTENT);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}
}