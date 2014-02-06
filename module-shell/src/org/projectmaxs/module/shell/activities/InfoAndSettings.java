package org.projectmaxs.module.shell.activities;

import org.projectmaxs.module.shell.R;
import org.projectmaxs.shared.global.util.SpannedUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

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
		final SpannableStringBuilder sb = new SpannableStringBuilder();
		final String appName = getResources().getString(R.string.app_name);
		sb.append(Html.fromHtml("<h1>" + appName + "</h1>"));
		sb.append(getResources().getString(R.string.version)).append('\n');
		sb.append(getResources().getString(R.string.copyright))
				.append(" (")
				.append(SpannedUtil.createAuthorsLink("module-shell",
						getResources().getString(R.string.authors))).append(")\n");
		sb.append('\n');
		sb.append(appName).append(' ').append(getResources().getText(R.string.gplv3)).append('\n');
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
}
