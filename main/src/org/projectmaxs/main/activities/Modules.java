package org.projectmaxs.main.activities;

import java.util.Collections;
import java.util.List;

import org.projectmaxs.main.ModuleRegistry;
import org.projectmaxs.main.R;
import org.projectmaxs.shared.ModuleInformation;

import android.app.Activity;
import android.content.ComponentName;
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

	ListView mModulesList;
	ModuleRegistry.ChangeListener mChangeListener = new ModuleRegistry.ChangeListener() {
		@Override
		public void dataChanged() {
			buildList();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modules);
		mModulesList = (ListView) findViewById(R.id.modulesList);
		buildList();
		ModuleRegistry.getInstance(this).addChangeListener(mChangeListener);
	}

	@Override
	protected void onStop() {
		super.onStop();
		ModuleRegistry.getInstance(this).removeChangeListener(mChangeListener);
	}

	private void buildList() {
		List<ModuleInformation> moduleInformationList = ModuleRegistry.getInstance(this).getAllModules();
		Collections.sort(moduleInformationList);
		ModuleInformationAdapter adapter = new ModuleInformationAdapter(this, R.id.modulesList, moduleInformationList);
		mModulesList.setAdapter(adapter);
	}

	class ModuleInformationAdapter extends ArrayAdapter<ModuleInformation> {
		final int mResource;
		final List<ModuleInformation> mData;

		public ModuleInformationAdapter(Context context, int resource, List<ModuleInformation> data) {
			super(context, resource, data);
			mResource = resource;
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
			intent.setComponent(new ComponentName(modulePackage, modulePackage + ".activities.InfoAndSettings"));
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
