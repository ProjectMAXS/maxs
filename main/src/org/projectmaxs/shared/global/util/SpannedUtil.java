/*
    This file is part of Project MAXS.

    MAXS and its modules is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MAXS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MAXS.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.projectmaxs.shared.global.util;

import org.projectmaxs.shared.global.GlobalConstants;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

public class SpannedUtil {

	public static final Spanned createLink(final String url, final String text) {
		return Html.fromHtml("<a href=\"" + url + "\">" + text + "</a>");
	}

	public static final Spanned createAuthorsLink(final String component, final String authors) {
		final String url = GlobalConstants.GIT_REPO_URL + "/" + component + "/AUTHORS";
		return createLink(url, authors);
	}

	public static final SpannableStringBuilder createdAboutDialog(Context context,
			String componentName, int appName,
			int version, int copyright, int authors, int gplv3) {
		final SpannableStringBuilder sb = new SpannableStringBuilder();

		final Resources resources = context.getResources();
		final PackageManagerUtil packageManagerUtil = PackageManagerUtil.getInstance(context);
		final String appNameString = resources.getString(appName);

		sb.append(Html.fromHtml("<h1>" + appNameString + "</h1>"));

		sb.append(Html.fromHtml("<b>" + "Version:" + "</b>")).append(' ')
				.append(packageManagerUtil.getMyVersionName()).append('\n');
		sb.append(Html.fromHtml("<b>" + "git:" + "</b>")).append(' ')
				.append(resources.getString(version)).append('\n');
		sb.append(Html.fromHtml("<b>" + "Version Code:" + "</b>")).append(' ')
				.append(String.valueOf(packageManagerUtil.getMyVersionCode())).append('\n');
		sb.append(resources.getString(copyright)).append(" (").append(
				SpannedUtil.createAuthorsLink(componentName, resources.getString(authors)))
				.append(")\n");
		sb.append(appNameString).append(' ').append(resources.getText(gplv3)).append('\n');

		return sb;
	}
}
