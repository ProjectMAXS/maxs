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

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.MultipleRecipientManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.XHTMLManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.CommandOrigin;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.transport.transform.TransformMessageContent;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.database.MessagesTable;
import org.projectmaxs.transport.xmpp.util.ConnectivityManagerUtil;
import org.projectmaxs.transport.xmpp.util.Constants;
import org.projectmaxs.transport.xmpp.util.XHTMLIMUtil;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class XMPPService {
	private static final Log LOG = Log.getLog();

	private static XMPPService sXMPPService;

	private final Set<StateChangeListener> mStateChangeListeners = Collections
			.synchronizedSet(new HashSet<StateChangeListener>());

	private final Settings mSettings;
	private final MessagesTable mMessagesTable;
	private final Context mContext;
	private final HandleTransportStatus mHandleTransportStatus;

	private XMPPStatus mXMPPStatus;
	private State mState = State.Disconnected;

	static {
		ServiceDiscoveryManager.setDefaultIdentity(new DiscoverInfo.Identity("client",
				GlobalConstants.NAME, "bot"));
	}

	private final Runnable mReconnectRunnable = new Runnable() {
		@Override
		public void run() {
			LOG.d("scheduleReconnect: calling tryToConnect");
			tryToConnect();
		}
	};

	/**
	 * Switch boolean to ensure that the disconnected(Connection) listeners are
	 * only run if there was a previous connected connection.
	 */
	private boolean mConnected = false;

	private ConnectionConfiguration mConnectionConfiguration;
	private XMPPConnection mConnection;
	private Handler mReconnectHandler;

	/**
	 * Get an XMPPService
	 * 
	 * Note that because of MemorizingTrustManager Context must be an instance of Application,
	 * Service or Activity. Therefore if you have an Context which is not Service or Activity, use
	 * getApplication().
	 * 
	 * @param context
	 *            as an instance of Application, Service or Activity.
	 * @return
	 */
	public static synchronized XMPPService getInstance(Context context) {
		if (sXMPPService == null) sXMPPService = new XMPPService(context);
		return sXMPPService;
	}

	private XMPPService(Context context) {
		XMPPEntityCapsCache.initialize(context);

		mContext = context;
		mSettings = Settings.getInstance(context);
		mMessagesTable = MessagesTable.getInstance(context);

		addListener(new HandleChatPacketListener(this));
		addListener(new HandleConnectionListener(this));
		addListener(new HandleMessagesListener(this));
		addListener(new XMPPPingManager(this));
		addListener(new XMPPFileTransfer(context));
		addListener(new XMPPDeliveryReceipts());
		addListener(new XMPPPrivacyList(mSettings));

		mHandleTransportStatus = new HandleTransportStatus(context);
		addListener(mHandleTransportStatus);
		XMPPRoster xmppRoster = new XMPPRoster(mSettings);
		addListener(xmppRoster);
		mXMPPStatus = new XMPPStatus(xmppRoster, context);
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

	public HandleTransportStatus getHandleTransportStatus() {
		return mHandleTransportStatus;
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

	public void newConnecitivytInformation(boolean connected, boolean networkTypeChanged) {
		// first disconnect if the network type changed and we are now connected
		// with an now unusable network
		if ((networkTypeChanged && isConnected()) || !connected) {
			LOG.d("newConnectivityInformation: calling disconnect() networkTypeChanged="
					+ networkTypeChanged + " connected=" + connected + " isConnected="
					+ isConnected());
			disconnect();
		}

		// if we have an connected network but we are not connected, connect
		if (connected && !isConnected()) {
			LOG.d("newConnectivityInformation: calling connect()");
			connect();
		} else if (!connected) {
			LOG.d("newConnectivityInformation: we are not connected any more, changing state to WaitingForNetwork");
			newState(State.WaitingForNetwork);
		}
	}

	public void send(org.projectmaxs.shared.global.Message message, CommandOrigin origin) {
		// If the origin is null, then we are receiving a broadcast message from
		// main. TODO document that origin can be null
		if (origin == null) {
			sendAsMessage(message, null, null);
			return;
		}

		String action = origin.getIntentAction();
		String originId = origin.getOriginId();
		String originIssuerInfo = origin.getOriginIssuerInfo();

		if (Constants.ACTION_SEND_AS_MESSAGE.equals(action)) {
			sendAsMessage(message, originIssuerInfo, originId);
		} else if (Constants.ACTION_SEND_AS_IQ.equals(action)) {
			sendAsIQ(message, originIssuerInfo, originId);
		} else {
			throw new IllegalStateException("XMPPService send: unknown action=" + action);
		}
	}

	public Connection getConnection() {
		return mConnection;
	}

	Context getContext() {
		return mContext;
	}

	private void sendAsMessage(org.projectmaxs.shared.global.Message message,
			String originIssuerInfo, String originId) {
		if (mConnection == null || !mConnection.isAuthenticated()) {
			// TODO I think that this could for example happen when the service
			// is not started but e.g. the SMS receiver get's a new message.
			LOG.i("sendAsMessage: Not connected, adding message to DB. mConnection=" + mConnection);
			mMessagesTable.addMessage(message, Constants.ACTION_SEND_AS_MESSAGE, originIssuerInfo,
					originId);
			return;
		}

		String to = originIssuerInfo;
		Message packet = new Message();
		packet.setType(Message.Type.chat);
		packet.setBody(TransformMessageContent.toString(message));
		packet.setThread(originId);

		List<String> toList = new LinkedList<String>();
		if (to == null) {
			// Broadcast to all masterJID resources
			for (String masterJid : mSettings.getMasterJids()) {
				Iterator<Presence> presences = mConnection.getRoster().getPresences(masterJid);
				while (presences.hasNext()) {
					Presence p = presences.next();
					String fullJID = p.getFrom();
					String resource = StringUtils.parseResource(fullJID);
					// Don't send messages to GTalk Android devices
					// It would be nice if there was a better way to detect
					// an Android gTalk XMPP client, but currently there is none
					if (resource != null && !resource.equals("")
							&& (!resource.startsWith("android"))) {
						toList.add(fullJID);
					}
				}
			}

			// (a)Smacks getRoster() is a little bit cranky at the moment. Besides everything XMPP
			// related being asynchronous, aSmacks getRoster is known to be often empty when the
			// method is called shortly after the login. We put some effort into the issue, but
			// until this is fixed, we have to deal with the situation that toList may be empty
			// sometimes. But since a broadcast should get delivered to every master JID, it is
			// not really a problem.
			for (String jid : mSettings.getMasterJids()) {
				boolean found = false;
				for (String toJid : toList) {
					if (StringUtils.parseBareAddress(toJid).equals(jid)) {
						found = true;
						break;
					}
				}
				// Add this master JID, if it isn't already contained in toList
				if (!found) toList.add(jid);
			}
		} else {
			toList.add(to);
		}

		boolean atLeastOneSupportsXHTMLIM = false;
		for (String jid : toList) {
			atLeastOneSupportsXHTMLIM = XHTMLManager.isServiceEnabled(mConnection, jid);
			if (atLeastOneSupportsXHTMLIM) break;
		}
		if (atLeastOneSupportsXHTMLIM)
			XHTMLIMUtil.addXHTMLIM(packet, TransformMessageContent.toFormatedText(message));

		try {
			MultipleRecipientManager.send(mConnection, packet, toList, null, null);
		} catch (XMPPException e1) {
			LOG.w("sendAsMessage: MultipleRecipientManager exception", e1);
		} catch (IllegalStateException e2) {
			if ("Not connected to server.".equals(e2.getMessage())) {
				LOG.i("sendAsMessage: Got IllegalStateException (Not connected), adding message to DB");
				mMessagesTable.addMessage(message, Constants.ACTION_SEND_AS_MESSAGE,
						originIssuerInfo, originId);
			} else {
				throw e2;
			}
		}
	}

	private void sendAsIQ(org.projectmaxs.shared.global.Message message, String originIssuerInfo,
			String issuerId) {
		// in a not so far future
	}

	protected void newMessageFromMasterJID(Message message) {
		String command = message.getBody();
		if (command == null) {
			LOG.e("newMessageFromMasterJID: empty body");
			return;
		}

		String issuerInfo = message.getFrom();
		LOG.d("newMessageFromMasterJID: command=" + command + " from=" + issuerInfo);

		Intent intent = new Intent(GlobalConstants.ACTION_PERFORM_COMMAND);
		CommandOrigin origin = new CommandOrigin(Constants.PACKAGE,
				Constants.ACTION_SEND_AS_MESSAGE, issuerInfo, null);
		intent.putExtra(TransportConstants.EXTRA_COMMAND, command);
		intent.putExtra(TransportConstants.EXTRA_COMMAND_ORIGIN, origin);
		intent.setClassName(GlobalConstants.MAIN_PACKAGE, TransportConstants.MAIN_TRANSPORT_SERVICE);
		ComponentName cn = mContext.startService(intent);
		if (cn == null) {
			LOG.e("newMessageFromMasterJID: could not start main transport service");
		}
	}

	protected void scheduleReconnect() {
		newState(State.WaitingForRetry);
		if (mReconnectHandler == null) mReconnectHandler = new Handler();
		mReconnectHandler.removeCallbacks(mReconnectRunnable);
		LOG.d("scheduleReconnect: scheduling reconnect in 10 seconds");
		mReconnectHandler.postDelayed(mReconnectRunnable, 10000);
	}

	private void newState(State newState) {
		newState(newState, "");
	}

	/**
	 * Notifies the StateChangeListeners about the new state and sets mState to
	 * newState. Does not add a log message.
	 * 
	 * @param newState
	 * @param reason
	 *            the reason for the new state (only used is newState is Disconnected)
	 */
	private void newState(State newState, String reason) {
		switch (newState) {
		case Connected:
			for (StateChangeListener l : mStateChangeListeners)
				l.connected(mConnection);
			mConnected = true;
			break;
		case Disconnected:
			for (StateChangeListener l : mStateChangeListeners) {
				l.disconnected(reason);
				if (mConnection != null && mConnected) l.disconnected(mConnection);
			}
			mConnected = false;
			break;
		case Connecting:
			for (StateChangeListener l : mStateChangeListeners)
				l.connecting();
			break;
		case Disconnecting:
			for (StateChangeListener l : mStateChangeListeners)
				l.disconnecting();
			break;
		case WaitingForNetwork:
			for (StateChangeListener l : mStateChangeListeners)
				l.waitingForNetwork();
			break;
		case WaitingForRetry:
			for (StateChangeListener l : mStateChangeListeners)
				l.waitingForRetry();
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
				mReconnectHandler.removeCallbacks(mReconnectRunnable);
				break;
			default:
				throw new IllegalStateException();
			}
			break;
		default:
			throw new IllegalStateException("changeState: Unkown state change combination. mState="
					+ mState + ", desiredState=" + desiredState);
		}
	}

	private synchronized void tryToConnect() {
		String failureReason = mSettings.checkIfReadyToConnect();
		if (failureReason != null) {
			LOG.w("tryToConnect: failureReason=" + failureReason);
			mHandleTransportStatus.setAndSendStatus("Unable to connect: " + failureReason);
			return;
		}

		if (isConnected()) {
			LOG.d("tryToConnect: already connected, nothing to do here");
			return;
		}
		if (!ConnectivityManagerUtil.hasDataConnection(mContext)) {
			LOG.d("tryToConnect: no data connection available");
			newState(State.WaitingForNetwork);
			return;
		}

		newState(State.Connecting);

		XMPPConnection connection;
		boolean newConnection = false;

		try {
			if (mConnectionConfiguration == null || mConnectionConfiguration != mSettings
			// We need to use an Application instance here, because some Context may not work.
					.getConnectionConfiguration(mContext)) {
				connection = new XMPPConnection(mSettings.getConnectionConfiguration(mContext));
				newConnection = true;
			} else {
				connection = mConnection;
			}
		} catch (XMPPException e) {
			String exceptionMessage = e.getMessage();
			// Schedule a reconnect on certain exception causes
			if ("DNS lookup failure".equals(exceptionMessage)) {
				LOG.w("tryToConnect: connection configuration failed. Scheduling reconnect. exceptionMessage="
						+ exceptionMessage);
				scheduleReconnect();
			} else {
				LOG.e("tryToConnect: connection configuration failed. New State: Disconnected", e);
				newState(State.Disconnected, e.getLocalizedMessage());
			}
			return;
		}

		LOG.d("tryToConnect: connect");
		try {
			connection.connect();
		} catch (Exception e) {
			// TODO see notice a few lines below at connection.logion() try/catch
			LOG.e("tryToConnect: Exception from connect()", e);
			scheduleReconnect();
			return;
		}

		if (!connection.isAuthenticated()) {
			try {
				connection.login(StringUtils.parseName(mSettings.getJid()),
						mSettings.getPassword(), "MAXS");
			} catch (Exception e) {
				// TODO we catch Exception instead of XMPPException here, since
				// Connection.sendPacket may send an IllegalStateException if not connected. This
				// could happen, and has happened, on login, when the connection goes down in the
				// meantime. Once sendPacket doesn't send an ISE when not connected, but a
				// XMPPException, this catch should be changed to XMPPException
				String exceptionMessage = e.getMessage();
				// Schedule a reconnect on certain exception causes
				if ("No response from the server.".equals(exceptionMessage)) {
					LOG.w("tryToConnect: login failed. Scheduling reconnect. exceptionMessage="
							+ exceptionMessage);
					scheduleReconnect();
				} else {
					LOG.e("tryToConnect: login failed. New State: Disconnected", e);
					newState(State.Disconnected, e.getLocalizedMessage());
				}
				return;
			}
		}
		// Login Successful

		mConnection = connection;

		try {
			if (newConnection) {
				for (StateChangeListener l : mStateChangeListeners) {
					l.newConnection(mConnection);
				}
			}

			newState(State.Connected);
		} catch (Exception e) {
			LOG.w("tryToConnect: Exception thrown by StateChangeListener", e);
			scheduleReconnect();
			return;
		}

		LOG.d("tryToConnect: successfully connected \\o/");
	}

	private synchronized void disconnectConnection() {
		if (mConnection != null) {
			if (mConnection.isConnected()) {
				newState(State.Disconnecting);
				LOG.d("disconnectConnection: disconnect start");
				mConnection.disconnect();
				LOG.d("disconnectConnection: disconnect stop");
			}
			newState(State.Disconnected);
		}
	}

}
