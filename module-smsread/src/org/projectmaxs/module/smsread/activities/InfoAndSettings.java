package org.projectmaxs.module.smsread.activities;

import org.projectmaxs.module.smsread.R;
import org.projectmaxs.shared.module.ModuleActivitiesUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class InfoAndSettings extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infoandsettings);
	}

	public void openSettings(View view) {
		startActivity(new Intent(this, Settings.class));
	}

	public void showAbout(View view) {
		ModuleActivitiesUtil.showAbout(this, "module-smsread", R.string.app_name,
				R.string.version, R.string.copyright, R.string.authors, R.string.gplv3,
				R.string.close);
	}

}
