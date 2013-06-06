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

package org.projectmaxs.main;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.MultipleRecipientManager;
import org.projectmaxs.main.xmpp.HandleChatPacketListener;
import org.projectmaxs.main.xmpp.HandleConnectionListener;
import org.projectmaxs.main.xmpp.XMPPRoster;
import org.projectmaxs.shared.UserMessage;
import org.projectmaxs.shared.util.Log;

public class XMPPService {
	private Set<StateChangeListener> mStateChangeListeners = new HashSet<StateChangeListener>();
	private State mState = State.Disconnected;
	private Settings mSettings;
	private ConnectionConfiguration mConnectionConfiguration;
	private XMPPConnection mConnection;
	private MAXSService mMAXSLocalService;

	/**
	 * Creates a new XMPPService instance. The XMPP connection will be
	 * automatically resumed if it was previously established
	 * 
	 * @param maxsLocalService
	 */
	public XMPPService(MAXSService maxsLocalService) {
		SmackAndroid.init(maxsLocalService);

		mSettings = Settings.getInstance(maxsLocalService);
		mMAXSLocalService = maxsLocalService;

		addListener(new HandleChatPacketListener(mMAXSLocalService, mSettings));
		addListener(new HandleConnectionListener(mMAXSLocalService));
		addListener(new XMPPRoster(mSettings));

		// Connect if the connection was previously established
		if (mSettings.getXMPPConnectionState()) connect();
	}

	public enum State {
		Connected, Connecting, Disconnecting, Disconnected, WaitingForNetwork, WaitingForRetry;
	}

	public State getCurrentState() {
		return mState;
	}

	public boolean isConnected() {
		return (getCurrentState() == State.Connected);
	}

	public void addListener(StateChangeListener listener) {
		mStateChangeListeners.add(listener);
	}

	public void removeListener(StateChangeListener listener) {
		mStateChangeListeners.remove(listener);
	}

	protected void connect() {
		changeState(XMPPService.State.Connected);
	}

	protected void disconnect() {
		changeState(XMPPService.State.Disconnected);
	}

	protected boolean send(UserMessage userMessage) {
		// TODO ID
		String to = userMessage.getTo();
		org.projectmaxs.shared.Message msg = userMessage.geMessage();

		Message packet = new Message();
		packet.setType(Message.Type.chat);
		packet.setBody(msg.getRawContent());

		if (to == null) {
			List<String> toList = new LinkedList<String>();
			for (String masterJid : mSettings.getMasterJids()) {
				Iterator<Presence> presences = mConnection.getRoster().getPresences(masterJid);
				while (presences.hasNext()) {
					Presence p = presences.next();
					String fullJID = p.getFrom();
					String resource = StringUtils.parseResource(fullJID);
					// Don't send messages to GTalk Android devices
					// It would be nice if there was a better way to detect
					// an Android gTalk XMPP client, but currently there is none
					if (resource != null && !resource.equals("") && (!resource.startsWith("android"))) {
						toList.add(fullJID);
					}
				}
			}
			try {
				MultipleRecipientManager.send(mConnection, packet, toList, null, null);
			} catch (XMPPException e) {
				Log.w("MultipleRecipientManager exception", e);
				return false;
			}
		}
		else {
			packet.setTo(to);
			mConnection.sendPacket(packet);
		}

		return true;
	}

	private void newState(State newState) {
		for (StateChangeListener l : mStateChangeListeners) {
			switch (newState) {
			case Connected:
				l.connected(mConnection);
				break;
			case Disconnected:
				l.disconnected(mConnection);
				break;
			case Connecting:
				l.connecting();
				break;
			case Disconnecting:
				l.disconnecting();
				break;
			default:
				break;
			}
		}
		switch (newState) {
		case Connected:
		case Connecting:
		case WaitingForNetwork:
		case WaitingForRetry:
			mSettings.setXMPPConnectionState(true);
			break;
		case Disconnected:
		case Disconnecting:
			mSettings.setXMPPConnectionState(false);
		default:
			break;
		}
		mState = newState;
	}

	private synchronized void changeState(State newState) {
		Log.d("XMPPService.changeState(): mState=" + mState + ", newState=" + newState);
		switch (mState) {
		case Connected:
			switch (newState) {
			case Connected:
				break;
			case Disconnected:
				disconnectConnection();
				newState(State.Disconnected);
				break;
			case WaitingForNetwork:
				disconnectConnection();
				newState(State.WaitingForNetwork);
				break;
			default:
				throw new IllegalStateException();
			}
			break;
		case Disconnected:
			switch (newState) {
			case Disconnected:
				break;
			case Connected:
				tryToConnect();
				break;
			case WaitingForNetwork:
				newState(newState);
				break;
			default:
				throw new IllegalStateException();
			}
			break;
		case WaitingForNetwork:
			switch (newState) {
			case WaitingForNetwork:
				break;
			case Connected:
				tryToConnect();
				break;
			case Disconnected:
				newState(State.Disconnected);
				break;
			default:
				throw new IllegalStateException();
			}
			break;
		case WaitingForRetry:
			switch (newState) {
			case WaitingForNetwork:
				break;
			case Connected:
				newState(State.Connected);
				break;
			case Disconnected:
				newState(newState);
				break;
			default:
				throw new IllegalStateException();
			}
			break;
		default:
			Log.w("XMPPService.changeState(): Unkown state change combination. mState=" + mState + ", newState="
					+ newState);
			// TODO enable this
			// throw new IllegalStateException();
		}
	}

	private void tryToConnect() {
		Log.d("XMPPService.tryToConnect()");
		newState(State.Connecting);

		XMPPConnection con;
		boolean newConnection = false;

		try {
			if (mConnectionConfiguration == null || mConnectionConfiguration != mSettings.getConnectionConfiguration()) {
				con = new XMPPConnection(mSettings.getConnectionConfiguration());
				newConnection = true;
			}
			else {
				con = mConnection;
			}
		} catch (XMPPException e) {
			Log.e("tryToConnect() connection configuration failed", e);
			// TODO try reconnect
			return;
		}

		try {
			con.connect();
		} catch (XMPPException e) {
			Log.e("Exception from connect()", e);
			return;
		}

		if (!con.isAuthenticated()) {
			try {
				con.login(mSettings.getJid(), mSettings.getPassword(), "MAXS");
			} catch (XMPPException e) {
				Log.e("tryToConnect() login failed", e);
				return;
			}
		}

		// Login Successful

		mConnection = con;

		if (newConnection) {
			for (StateChangeListener l : mStateChangeListeners) {
				l.newConnection(mConnection);
			}
		}

		// TODO handle offline messages as StateChangeListener
		// TODO ping failed listener as StateChangeListener

		newState(State.Connected);

		// Send the first presence *after* all StateChangeListeners have been
		// called, in order to do their work prior the user's firsts knowledge
		// of the online/available state
		mConnection.sendPacket(new Presence(Presence.Type.available));
	}

	private void disconnectConnection() {
		if (mConnection != null) {
			if (mConnection.isConnected()) {
				newState(State.Disconnecting);
				// TODO better disconnect handle (e.g. in extra thread)
				mConnection.disconnect();
			}
		}
	}
}
