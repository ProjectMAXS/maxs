package org.projectmaxs.main.activities;

import java.util.List;

import org.projectmaxs.main.ModuleRegistry;
import org.projectmaxs.main.R;
import org.projectmaxs.shared.mainmodule.ModuleInformation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Modules extends Activity {

	private ListView mModulesList;

	private List<ModuleInformation> mModuleInformationList;
	private ModuleInformationAdapter mModuleInformationAdapter;
	private final ModuleRegistry.ChangeListener mChangeListener = new ModuleRegistry.ChangeListener() {

		@Override
		public void moduleRegistred(final ModuleInformation module) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mModuleInformationList.add(module);
					mModuleInformationAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void moduleUnregistred(final ModuleInformation module) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mModuleInformationList.remove(module);
					mModuleInformationAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modules);
		mModulesList = (ListView) findViewById(R.id.modulesList);
		mModuleInformationList = ModuleRegistry.getInstance(this).getCopyAddListener(mChangeListener);
		mModuleInformationAdapter = new ModuleInformationAdapter(this, mModuleInformationList);
		mModulesList.setAdapter(mModuleInformationAdapter);
	}

	@Override
	protected void onStop() {
		super.onStop();
		ModuleRegistry.getInstance(this).removeChangeListener(mChangeListener);
	}

	class ModuleInformationAdapter extends ArrayAdapter<ModuleInformation> {
		final List<ModuleInformation> mData;

		public ModuleInformationAdapter(Context context, List<ModuleInformation> data) {
			super(context, R.layout.modules_listview_row, data);
			mData = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ModuleInformation mi = mData.get(position);
			String moduleName = mi.getModuleName();
			String modulePackage = mi.getModulePackage();
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = Modules.this.getLayoutInflater();
				row = inflater.inflate(R.layout.modules_listview_row, parent, false);
			}

			final Intent intent = new Intent();
			intent.setClassName(modulePackage, modulePackage + ".activities.InfoAndSettings");
			row.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(intent);
				}
			});
			final TextView textModuleName = (TextView) row.findViewById(R.id.textModuleName);
			final TextView textModulePackage = (TextView) row.findViewById(R.id.textModulePackage);
			textModuleName.setText(moduleName);
			textModulePackage.setText(modulePackage);

			return row;
		}
	}
}
