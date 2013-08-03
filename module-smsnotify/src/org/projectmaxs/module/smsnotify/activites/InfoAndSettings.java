package org.projectmaxs.module.smsnotify.activites;

import org.projectmaxs.module.smsnotify.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class InfoAndSettings extends PreferenceActivity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.infoandsettings);
	}
}
