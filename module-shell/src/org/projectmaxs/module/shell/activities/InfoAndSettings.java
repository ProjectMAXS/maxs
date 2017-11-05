package org.projectmaxs.module.shell.activities;

import org.projectmaxs.module.shell.R;
import org.projectmaxs.shared.global.util.ActivityUtil;
import org.projectmaxs.shared.global.util.SpannedUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
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
		SpannableStringBuilder sb = SpannedUtil.createdAboutDialog(this, "module-shell",
				R.string.app_name, R.string.version, R.string.copyright, R.string.authors,
				R.string.gplv3);
		sb.append('\n');
		sb.append(Html.fromHtml(
// @formatter:off
"<h1>Open Source</h1>" +
"<h2>RootCommands</h2>" +
"<a href=\"https://github.com/dschuermann/root-commands\">https://github.com/dschuermann/root-commands</a><br>" +
"<br>" +
"Copyright © 2013 Dominik Schürmann <dominik@dominikschuermann.de><br>" +
"Copyright © 2012 Stephen Erickson, Chris Ravenscroft, Adam Shanks (RootTools)<br>" +
"Apache License, Version 2.0<br>" +
"<h1>License Links</h1>" +
"&#8226; <a href=\"http://www.apache.org/licenses/LICENSE-2.0\">Apache License, Version 2.0</a><br>"
// @formatter:on
				));

		ActivityUtil.showSimpleTextView(this, sb, R.string.close);
	}
}
