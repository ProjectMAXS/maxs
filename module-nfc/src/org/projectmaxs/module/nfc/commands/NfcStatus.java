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

package org.projectmaxs.module.nfc.commands;

import java.util.LinkedList;
import java.util.List;

import org.projectmaxs.module.nfc.ModuleService;
import org.projectmaxs.shared.global.Message;
import org.projectmaxs.shared.global.messagecontent.AbstractElement;
import org.projectmaxs.shared.global.messagecontent.CommandHelp.ArgType;
import org.projectmaxs.shared.mainmodule.Command;
import org.projectmaxs.shared.module.MAXSModuleIntentService;
import org.projectmaxs.shared.module.SubCommand;
import org.projectmaxs.shared.module.messagecontent.BooleanElement;

import android.annotation.TargetApi;
import android.nfc.NfcAdapter;
import android.os.Build;

public class NfcStatus extends SubCommand {

	public NfcStatus() {
		super(ModuleService.NFC, "status", true);
		setHelp(ArgType.NONE, "Show the current status of the nfc adapter");
	}

	@TargetApi(16)
	@Override
	public Message execute(String arguments, Command command, MAXSModuleIntentService service) {
		final NfcAdapter adapter = NfcAdapter.getDefaultAdapter(service);
		if (adapter == null)
			return new Message("NFC Adapter is null. Maybe this device does not support nfc?");

		List<AbstractElement> elements = new LinkedList<AbstractElement>();
		elements.add(BooleanElement.enabled("NFC Adapter is %1$s", "nfc_adapter_enabled",
				adapter.isEnabled(), service));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			elements.add(BooleanElement.enabled("NFC NDEF Push (Android Beem) is %1$s",
					"nfc_ndef_push_enabled", adapter.isNdefPushEnabled(), service));
		}
		return new Message(elements);
	}
}
