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

package org.projectmaxs.module.notificationpolicyaccess.commands;

import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SubCommand;
import org.projectmaxs.shared.module.SupraCommand;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;

/**
 * The volume set family if commands is in module-notificationpolicyaccess because starting with
 * Android M (API 23) changing the volume when in DND mode requires the notification policy access
 * permission.
 */
public abstract class AbstractVolumeSetCommand extends SubCommand {

	public static final SupraCommand VOLUME_SET = new SupraCommand("volume-set");

	protected final String mVolumeStreamName;
	protected final int mStream;

	/**
	 * Create a new non-default volume-set command.
	 *
	 * @param volumeStreamName the name of the command
	 */
	public AbstractVolumeSetCommand(String volumeStreamName) {
		this(volumeStreamName, false);
	}

	/**
	 * Create a new volume-set command. The default command can only be set exactly once.
	 *
	 * @param volumeStreamName the name of the command
	 * @param isDefaultWithArguments if this is the default command
	 */
	public AbstractVolumeSetCommand(String volumeStreamName, boolean isDefaultWithArguments) {
		super(VOLUME_SET, volumeStreamName, false, isDefaultWithArguments);
		mVolumeStreamName = volumeStreamName;
		mStream = streamStringToInt(volumeStreamName);
		setHelp(ArgType.PERCENTAGE, "The " + volumeStreamName + " volume in percent [0, 100]");
	}

	@Override
	public final Message execute(String arguments, Command command,
			MAXSModuleIntentService service) {
		final AudioManager audioManager = (AudioManager) service
				.getSystemService(Context.AUDIO_SERVICE);

		if (audioManager.isVolumeFixed()) {
			return new Message("Can not modify volume as it is fixed by the device");
		}

		// TODO: Strip percentage sign (or every none number) after first numbers in arguments.

		int volume;
		try {
			volume = Integer.parseInt(arguments);
		} catch (NumberFormatException e) {
			return new Message(
					"Volume percentage argument must be within 0 and 100 (was: " + arguments + ')');
		}

		if (volume < 0 || volume > 100) {
			return new Message("Can not set volume to " + volume
					+ ", as it must be within the range of 0 and 100");
		}

		final NotificationManager notificationManager = (NotificationManager) service
				.getSystemService(Context.NOTIFICATION_SERVICE);

		final int currentInterruptionFilter = notificationManager.getCurrentInterruptionFilter();

		if (currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_NONE
				&& !notificationManager.isNotificationPolicyAccessGranted()) {
			return new Message(
					"Require access to notification policy in order to change the volume since the device is in DND mode. Please issue the following MAXS command: notification-policy request");
		}

		int streamMax = audioManager.getStreamMaxVolume(mStream);
		int index = (int) (volume / 100.0) * streamMax;
		audioManager.setStreamVolume(mStream, index, 0);

		return new Message("Set " + mVolumeStreamName + " volume to " + volume + '%');
	}

	static int streamStringToInt(String stream) {
		int res;
		switch (stream) {
		case VolumeSetAlarm.STREAM_NAME:
			res = AudioManager.STREAM_ALARM;
			break;
		case VolumeSetDtmf.STREAM_NAME:
			res = AudioManager.STREAM_DTMF;
			break;
		case VolumeSetMusic.STREAM_NAME:
			res = AudioManager.STREAM_MUSIC;
			break;
		case VolumeSetNotification.STREAM_NAME:
			res = AudioManager.STREAM_NOTIFICATION;
			break;
		case VolumeSetRing.STREAM_NAME:
			res = AudioManager.STREAM_RING;
			break;
		case VolumeSetSystem.STREAM_NAME:
			res = AudioManager.STREAM_SYSTEM;
			break;
		case VolumeSetVoiceCall.STREAM_NAME:
			res = AudioManager.STREAM_VOICE_CALL;
			break;
		default:
			throw new IllegalArgumentException("Unknown stream: " + stream);
		}
		return res;
	}
}
