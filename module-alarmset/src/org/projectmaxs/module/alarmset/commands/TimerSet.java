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

package org.projectmaxs.module.alarmset.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.projectmaxs.module.alarmset.ModuleService;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SubCommand;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.provider.AlarmClock;

@TargetApi(19)
public class TimerSet extends SubCommand {

	public TimerSet() {
		super(ModuleService.TIMER, "set", false, true);
		setHelp("'<number>[(s|m|h)] [<timer description>] or '[HH:]mm:ss [<timer description>]'",
				"Set a new timer");
		setRequiresArgument();
	}

	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			return new Message("Timer needs a minimum API level of 19 (KitKat)");

		String duration;
		String timerDescription;
		int spaceIndex = arguments.indexOf(' ');
		if (spaceIndex == -1) {
			duration = arguments;
			timerDescription = null;
		} else {
			duration = arguments.substring(0, spaceIndex);
			timerDescription = arguments.substring(spaceIndex);
		}
		Intent intent = new Intent(AlarmClock.ACTION_SET_TIMER);
		int seconds = toSeconds(duration);
		intent.putExtra(AlarmClock.EXTRA_LENGTH, seconds);

		if (timerDescription == null) timerDescription = "Created by MAXS";
		intent.putExtra(AlarmClock.EXTRA_MESSAGE, timerDescription);

		intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		service.startActivity(intent);

		return new Message("Timer set");
	}

	private static final List<PatternFactor> PATTERN_LIST = new LinkedList<PatternFactor>();

	static {
		PATTERN_LIST.add(new PatternFactor(Pattern.compile("([0-9]+)s"), 1));
		PATTERN_LIST.add(new PatternFactor(Pattern.compile("([0-9]+)m"), 60));
		PATTERN_LIST.add(new PatternFactor(Pattern.compile("([0-9]+)h"), 60 * 60));
	}

	private static final int toSeconds(String string) {
		if (SharedStringUtil.isPositiveInteger(string)) return Integer.parseInt(string);

		if (string.indexOf(':') >= 0) {
			int res = 0;
			String[] fields = string.split(":");
			for (int i = 0; i < fields.length; i++) {
				int value = Integer.parseInt(fields[i]);
				res += value * (10 * i);
			}
			return res;
		}

		for (PatternFactor pf : PATTERN_LIST) {
			Matcher matcher = pf.mPattern.matcher(string);
			if (matcher.matches()) {
				String field = matcher.group(1);
				int value = Integer.parseInt(field);
				return value * pf.mFactor;
			}
		}
		throw new IllegalArgumentException("Argument '" + string
				+ "' is not in a valid format. Use either '<number>[(s|m|h)]' or 'HH:mm:ss'.");
	}

	private static class PatternFactor {
		final Pattern mPattern;
		final Integer mFactor;

		PatternFactor(Pattern pattern, Integer factor) {
			mPattern = pattern;
			mFactor = factor;
		}
	}
}
