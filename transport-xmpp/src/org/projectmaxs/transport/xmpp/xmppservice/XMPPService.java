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

package org.projectmaxs.transport.xmpp.xmppservice;

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
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.database.MessagesTable;
import org.projectmaxs.transport.xmpp.util.Constants;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

public class XMPPService {
	private static final Log LOG = Log.getLog();

	private static XMPPService sXMPPService;

	private final Set<StateChangeListener> mStateChangeListeners = new HashSet<StateChangeListener>();
	private final Handler mHandler = new Handler();

	private final Settings mSettings;
	private final MessagesTable mMessagesTable;
	private final Context mContext;
	private final ConnectivityManager mConnectivityManager;

	private XMPPStatus mXMPPStatus;
	private State mState = State.Disconnected;
	private ConnectionConfiguration mConnectionConfiguration;
	private XMPPConnection mConnection;
	private Runnable mReconnectRunnable;

	public static synchronized XMPPService getInstance(Context context) {
		if (sXMPPService == null) sXMPPService = new XMPPService(context);
		return sXMPPService;
	}

	private XMPPService(Context context) {
		SmackAndroid.init(context);

		XMPPEntityCapsCache.initialize(context);

		mContext = context;
		mSettings = Settings.getInstance(context);
		mMessagesTable = MessagesTable.getInstance(context);
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		addListener(new HandleChatPacketListener(this));
		addListener(new HandleConnectionListener(this));
		addListener(new HandleMessagesListener(this));
		addListener(new XMPPPingManager(this));
		addListener(new XMPPFileTransfer(context));

		XMPPRoster xmppRoster = new XMPPRoster(mSettings);
		addListener(xmppRoster);
		mXMPPStatus = new XMPPStatus(xmppRoster);
		addListener(mXMPPStatus);
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

	public void connect() {
		changeState(XMPPService.State.Connected);
	}

	public void disconnect() {
		changeState(XMPPService.State.Disconnected);
	}

	public void reconnect() {
		disconnect();
		connect();
	}

	public void setStatus(String status) {
		mXMPPStatus.setStatus(status);
	}

	public Context getContext() {
		return mContext;
	}

	protected void sendAsMessage(org.projectmaxs.shared.global.Message message, String originIssuerInfo, String originId) {
		if (mConnection == null || !mConnection.isAuthenticated()) {
			LOG.w("sendAsMessage: Not connected, adding message to DB");
			mMessagesTable.addMessage(message, Constants.ACTION_SEND_AS_MESSAGE, originIssuerInfo, originId);
			return;
		}

		String to = originIssuerInfo;
		Message packet = new Message();
		packet.setType(Message.Type.chat);
		packet.setBody(message.getRawContent());
		packet.setThread(originId);

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
				LOG.w("sendAsMessage: MultipleRecipientManager exception", e);
				return;
			}
		}
		else {
			packet.setTo(to);
			mConnection.sendPacket(packet);
		}

		return;
	}

	protected void sendAsIQ(org.projectmaxs.shared.global.Message message, String originIssuerInfo, String issuerId) {
		// TODO
	}

	protected void newConnecitivytInformation(boolean connected, boolean networkTypeChanged) {
		// first disconnect if the network type changed and we are now connected
		// with an now unusable network
		if ((networkTypeChanged && isConnected()) || !connected) {
			disconnect();
		}

		// if we have an connected network but we are not connected, connect
		if (connected && !isConnected()) {
			connect();
		}
		else if (!connected) {
			newState(State.WaitingForNetwork);
		}
	}

	protected void newMessageFromMasterJID(Message message) {
		String body = message.getBody();
		if (body == null) return;

		String[] splitedBody = body.split(" ");
		String command = splitedBody[0];

		String subCmd = null;
		if (splitedBody.length > 1) subCmd = splitedBody[1];

		String args = null;
		if (splitedBody.length > 2) {
			StringBuilder sb = new StringBuilder();
			for (int i = 2; i < splitedBody.length; i++)
				sb.append(splitedBody[i]);
			args = sb.toString();
		}
		String from = message.getFrom();
		// TODO mMAXSLocalService.performCommand(command, subCmd, args,
		// MAXSService.CommandOrigin.XMPP_MESSAGE, null, from);
	}

	protected void scheduleReconnect() {
		newState(State.WaitingForRetry);
		LOG.d("scheduleReconnect: scheduling reconnect in 10 seconds");
		mHandler.removeCallbacks(mReconnectRunnable);
		mReconnectRunnable = new Runnable() {
			@Override
			public void run() {
				LOG.d("scheduleReconnect: calling tryToConnect");
				tryToConnect();
			}
		};
		mHandler.postDelayed(mReconnectRunnable, 10000);
	}

	/**
	 * Notifies the StateChangeListeners about the new state and sets mState to
	 * newState. Does not add a log message.
	 * 
	 * @param newState
	 */
	private void newState(State newState) {
		switch (newState) {
		case Connected:
			for (StateChangeListener l : mStateChangeListeners)
				l.connected(mConnection);
			break;
		case Disconnected:
			// don't call listeners if there was never a connection
			if (mConnection == null) break;
			for (StateChangeListener l : mStateChangeListeners)
				l.disconnected(mConnection);
			break;
		case Connecting:
			for (StateChangeListener l : mStateChangeListeners)
				l.connecting();
			break;
		case Disconnecting:
			for (StateChangeListener l : mStateChangeListeners)
				l.disconnecting();
			break;
		default:
			break;
		}
		mState = newState;
	}

	private synchronized void changeState(State desiredState) {
		LOG.d("changeState: mState=" + mState + ", desiredState=" + desiredState);
		switch (mState) {
		case Connected:
			switch (desiredState) {
			case Connected:
				break;
			case Disconnected:
				disconnectConnection();
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
			switch (desiredState) {
			case Disconnected:
				break;
			case Connected:
				tryToConnect();
				break;
			case WaitingForNetwork:
				newState(State.WaitingForNetwork);
				break;
			default:
				throw new IllegalStateException();
			}
			break;
		case WaitingForNetwork:
			switch (desiredState) {
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
			switch (desiredState) {
			case WaitingForNetwork:
				newState(State.WaitingForNetwork);
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
		default:
			LOG.w("changeState: Unkown state change combination. mState=" + mState + ", desiredState=" + desiredState);
			// TODO enable this
			// throw new IllegalStateException();
		}
	}

	private synchronized void tryToConnect() {
		String failureReason = null;
		if (mSettings.getPassword().length() == 0) failureReason = "Password not set or empty";
		if (mSettings.getJid().length() == 0) failureReason = "JID not set or empty";
		if (mSettings.getMasterJidCount() == 0) failureReason = "Master JID(s) not configured";
		if (failureReason != null) {
			LOG.w("tryToConnect: failureReason=" + failureReason);
			// TODO intent ACTION_SERVICE_STATUS
			return;
		}

		if (isConnected()) {
			LOG.d("tryToConnect: already connected, nothing to do here");
			return;
		}
		if (dataConnectionAvailable()) {
			LOG.d("tryToConnect: no data connection available");
			newState(State.WaitingForNetwork);
			return;
		}

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
			LOG.e("tryToConnect: connection configuration failed", e);
			mState = State.Disconnected;
			return;
		}

		LOG.d("tryToConnect: connect");
		try {
			con.connect();
		} catch (XMPPException e) {
			LOG.e("tryToConnect: Exception from connect()", e);
			scheduleReconnect();
			return;
		}

		if (!con.isAuthenticated()) {
			try {
				con.login(mSettings.getJid(), mSettings.getPassword(), "MAXS");
			} catch (XMPPException e) {
				LOG.e("tryToConnect: login failed", e);
				mState = State.Disconnected;
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

		LOG.d("tryToConnect: successfully connected \\o/");
		newState(State.Connected);
	}

	private synchronized void disconnectConnection() {
		if (mConnection != null) {
			if (mConnection.isConnected()) {
				newState(State.Disconnecting);
				// TODO better disconnect handle (e.g. in extra thread)
				LOG.d("disconnectConnection: disconnect start");
				mConnection.disconnect();
				LOG.d("disconnectConnection: disconnect stop");
			}
			newState(State.Disconnected);
		}
	}

	private boolean dataConnectionAvailable() {
		NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
		if (activeNetwork == null) return false;
		return activeNetwork.isConnected();
	}

}
