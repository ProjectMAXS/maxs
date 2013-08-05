package org.projectmaxs.module.bluetoothadmin.activities;

import org.projectmaxs.module.bluetoothadmin.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class InfoAndSettings extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.infoandsettings);
	}

}
