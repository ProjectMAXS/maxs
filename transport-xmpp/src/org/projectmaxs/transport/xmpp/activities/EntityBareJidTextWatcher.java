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

package org.projectmaxs.transport.xmpp.activities;

import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.util.JidUtil;
import org.jxmpp.stringprep.XmppStringprepException;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class EntityBareJidTextWatcher implements TextWatcher {

	private final EditText jidEditText;

	private final OnValidBareJidCallback onValidBareJidCallback;
	private final OnInvalidJidCallback onInvalidJidCallback;
	private final OnEmptyCallback onEmptyCallback;

	private enum State {
		validJid,
		invalidJid,
		empty,
		;
	}

	private State state;

	public static void watch(EditText jidEditText, OnValidBareJidCallback onValidBareJidCallback,
			OnInvalidJidCallback onInvalidJidCallback) {
		new EntityBareJidTextWatcher(jidEditText, onValidBareJidCallback, onInvalidJidCallback,
				null);
	}

	private EntityBareJidTextWatcher(EditText jidEditText,
			OnValidBareJidCallback onValidBareJidCallback,
			OnInvalidJidCallback onInvalidJidCallback, OnEmptyCallback onEmptyCallback) {
		this.jidEditText = jidEditText;
		this.onValidBareJidCallback = onValidBareJidCallback;
		this.onInvalidJidCallback = onInvalidJidCallback;
		this.onEmptyCallback = onEmptyCallback;

		jidEditText.addTextChangedListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	}

	@Override
	public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
	}

	@Override
	public final void afterTextChanged(Editable editable) {
		final String stringOfEditable = editable.toString();
		final State currentState = determineState(stringOfEditable);

		if (state == currentState && currentState != State.validJid) {
			// Only abort here if the state hasn't changed and if it's not a valid JID. We want to
			// get notified when then user makes changes to an already valid JID that result in
			// anther valid JID.
			return;
		}

		state = currentState;

		switch (state) {
		case empty:
			jidEditText.setBackgroundColor(Color.WHITE);
			if (onEmptyCallback != null) {
				onEmptyCallback.onEmpty(editable);
			}
			break;
		case invalidJid:
			jidEditText.setBackgroundColor(Color.RED);
			if (onInvalidJidCallback != null) {
				onInvalidJidCallback.onInvalidJid(editable);
			}
			break;
		case validJid:
			jidEditText.setBackgroundColor(Color.GREEN);
			EntityBareJid entityBareJid;
			try {
				entityBareJid = JidCreate.entityBareFrom(stringOfEditable);
			} catch (XmppStringprepException e) {
				throw new AssertionError(e);
			}
			if (onValidBareJidCallback != null) {
				onValidBareJidCallback.onValidBareJid(entityBareJid, editable);
			}
			break;
		}
	}

	private static State determineState(CharSequence cs) {
		if (cs.length() == 0) {
			return State.empty;
		} else if (JidUtil.isTypicalValidEntityBareJid(cs)) {
			return State.validJid;
		} else {
			return State.invalidJid;
		}
	}

	public interface OnValidBareJidCallback {
		void onValidBareJid(EntityBareJid entityBareJid, Editable editable);
	}

	public interface OnInvalidJidCallback {
		void onInvalidJid(Editable editable);
	}

	public interface OnEmptyCallback {
		void onEmpty(Editable editable);
	}
}
