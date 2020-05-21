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

import java.io.File;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.ConnectionException;
import org.jivesoftware.smack.SmackException.FeatureNotSupportedException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.debugger.JulDebugger;
import org.jivesoftware.smack.debugger.SmackDebugger;
import org.jivesoftware.smack.debugger.SmackDebuggerFactory;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterLoadedListener;
import org.jivesoftware.smack.roster.RosterUtil;
import org.jivesoftware.smack.roster.rosterstore.DirectoryRosterStore;
import org.jivesoftware.smack.roster.rosterstore.RosterStore;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.Async;
import org.jivesoftware.smack.util.Async.ThrowingRunnable;
import org.jivesoftware.smack.util.DNSUtil;
import org.jivesoftware.smack.util.rce.RemoteConnectionException;
import org.jivesoftware.smackx.address.MultipleRecipientManager;
import org.jivesoftware.smackx.carbons.packet.CarbonExtension;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.EntityJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.util.FileUtil;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.CommandOrigin;
import org.projectmaxs.shared.maintransport.CurrentStatus;
import org.projectmaxs.shared.maintransport.TransportConstants;
import org.projectmaxs.shared.transport.PrngFixes;
import org.projectmaxs.shared.transport.transform.TransformMessageContent;
import org.projectmaxs.transport.xmpp.Settings;
import org.projectmaxs.transport.xmpp.database.MessagesTable;
import org.projectmaxs.transport.xmpp.smack.provider.MAXSElementProvider;
import org.projectmaxs.transport.xmpp.smack.stanza.MAXSElement;
import org.projectmaxs.transport.xmpp.util.ConnectivityManagerUtil;
import org.projectmaxs.transport.xmpp.util.Constants;
import org.projectmaxs.transport.xmpp.util.XHTMLIMUtil;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class XMPPService {
	private static final Log LOG = Log.getLog();

	@SuppressLint("StaticFieldLeak")
	private static XMPPService sXMPPService;

	private final Set<StateChangeListener> mStateChangeListeners = new CopyOnWriteArraySet<StateChangeListener>();

	private final Settings mSettings;
	private final MessagesTable mMessagesTable;
	private final Context mContext;
	private final HandleTransportStatus mHandleTransportStatus;

	private XMPPStatus mXMPPStatus;
	private State mState = State.Disconnected;

	static {
		// Remove PrngFixes.apply() once MAXS is Android API 19 (4.4, Kitkat) or higher.
		PrngFixes.apply();

		ServiceDiscoveryManager.setDefaultIdentity(
				new DiscoverInfo.Identity("client", GlobalConstants.HUMAN_READABLE_NAME, "bot"));
		// TODO This is not really needed, but for some reason the static initializer block of
		// LastActivityManager is not run. This could be a problem caused by aSmack together with
		// dalvik, as the initializer is run on Smack's test cases.
		LastActivityManager.setEnabledPerDefault(true);
		// Some network types, especially GPRS or EDGE is rural areas have a very slow response
		// time. Smack's default packet reply timeout of 5 seconds is way to low for such networks,
		// so we increase it to 2 minutes.
		// This value must also be greater then the highest returned bundle and defer value.
		SmackConfiguration.setDefaultReplyTimeout(2 * 60 * 1000);

		// @formatter:off
		SmackConfiguration.addDisabledSmackClasses(
				  "org.jivesoftware.smack.ReconnectionManager"
				, "org.jivesoftware.smack.util.dns.javax"
				, "org.jivesoftware.smack.util.dns.dnsjava"
				, "org.jivesoftware.smack.sasl.javax"
				, "org.jivesoftware.smack.legacy"
				, "org.jivesoftware.smack.java7"
				, "org.jivesoftware.smackx.eme"
				, "org.jivesoftware.smackx.gcm"
				, "org.jivesoftware.smackx.httpfileupload"
				, "org.jivesoftware.smackx.hoxt"
				, "org.jivesoftware.smackx.iot"
				, "org.jivesoftware.smackx.json"
				, "org.jivesoftware.smackx.muc"
				, "org.jivesoftware.smackx.omemo"
				, "org.jivesoftware.smackx.xdata"
				, "org.jivesoftware.smackx.xdatalayout"
				, "org.jivesoftware.smackx.xdatavalidation"
				);
		// @formatter:on

		SmackConfiguration.setDefaultSmackDebuggerFactory(new SmackDebuggerFactory() {
			@Override
			public SmackDebugger create(XMPPConnection connection) throws IllegalArgumentException {
				return new JulDebugger(connection);
			}
		});
		MAXSElementProvider.setup();
	}

	private final Runnable mReconnectRunnable = new Runnable() {
		@Override
		public void run() {
			LOG.d("scheduleReconnect: calling tryToConnect");
			tryToConnect();
		}
	};

	/**
	 * Switch boolean to ensure that the disconnected(XMPPConnection) listeners are
	 * only run if there was a previous connected connection.
	 */
	private boolean mConnected = false;

	private XMPPTCPConnectionConfiguration mConnectionConfiguration;
	private XMPPTCPConnection mConnection;
	private Handler mReconnectHandler;

	private int mReconnectionAttemptCount;

	/**
	 * Get an XMPPService
	 * 
	 * Note that because of MemorizingTrustManager Context must be an instance of Application,
	 * Service or Activity. Therefore if you have an Context which is not Service or Activity, use
	 * getApplication().
	 * 
	 * @param context
	 *            as an instance of Application, Service or Activity.
	 * @return The XMPPService instance.
	 */
	public static synchronized XMPPService getInstance(Context context) {
		// TODO: We shoud probably use the application context here: context.getApplicationContext().
		if (sXMPPService == null) sXMPPService = new XMPPService(context);
		return sXMPPService;
	}

	private XMPPService(Context context) {
		XMPPVersion.initialize(context);
		XMPPBundleAndDefer.initialize(context);

		mContext = context;
		mSettings = Settings.getInstance(context);
		mMessagesTable = MessagesTable.getInstance(context);

		// SendStanzaDatabaseHandler should be the first
		addListener(new SendStanzaDatabaseHandler(this));
		addListener(new HandleChatPacketListener(this));
		addListener(new HandleConnectionListener(this));
		addListener(new HandleMessagesListener(this));
		addListener(new XMPPPingManager(this));
		addListener(new XMPPFileTransfer(context));
		addListener(new XMPPPrivacyList(mSettings));

		mHandleTransportStatus = new HandleTransportStatus(context);
		addListener(mHandleTransportStatus);
		XMPPRoster xmppRoster = new XMPPRoster(mSettings);
		addListener(xmppRoster);
		mXMPPStatus = new XMPPStatus(xmppRoster, context);
		addListener(mXMPPStatus);
	}

	public static enum State {
		Connected, Connecting, Disconnecting, Disconnected, InstantDisconnected, WaitingForNetwork, WaitingForRetry;
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
		synchronized (mStateChangeListeners) {
			mStateChangeListeners.remove(listener);
		}
	}

	public void connect() {
		changeState(XMPPService.State.Connected);
	}

	public void disconnect() {
		changeState(XMPPService.State.Disconnected);
	}

	public void instantDisconnect() {
		changeState(XMPPService.State.InstantDisconnected);
	}

	public void reconnect() {
		disconnect();
		connect();
	}

	public void setStatus(CurrentStatus status) {
		mXMPPStatus.setStatus(status);
	}

	public void networkDisconnected() {
		changeState(State.WaitingForNetwork);
	}

	public void send(Jid to, String body) {
		switch (mState) {
		case Disconnected:
		case Disconnecting:
			LOG.w("Transport is disconnected, not going to send message to " + to);
			return;
		default:
			break;
		}
		if (!shouldUseXmppConnection()) {
			LOG.w("Connection is not connected and no resumption possible, not going to send message to "
					+ to);
			return;
		}
		Message message = new Message();
		message.setTo(to);
		message.setBody(body);
		try {
			mConnection.sendStanza(message);
		} catch (InterruptedException | NotConnectedException e) {
			LOG.w("send", e);
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

	public XMPPConnection getConnection() {
		return mConnection;
	}

	public boolean fastPingServer() {
		if (mConnection == null) return false;
		PingManager pingManager = PingManager.getInstanceFor(mConnection);
		XMPPBundleAndDefer.disableBundleAndDefer();
		try {
			return pingManager.pingMyServer(false, 1500);
		} catch (InterruptedException | NotConnectedException e) {
			return false;
		} finally {
			XMPPBundleAndDefer.enableBundleAndDefer();
		}
	}

	public void notifyAboutNewMasterAddress(final EntityBareJid newMasterAddress) {
		final XMPPConnection connection = getConnection();
		if (connection == null || !connection.isAuthenticated()) {
			return;
		}

		final Roster roster = Roster.getInstanceFor(connection);

		Async.go(new ThrowingRunnable() {
			@Override
			public void runOrThrow() throws NotLoggedInException, NotConnectedException,
					FeatureNotSupportedException, InterruptedException {
				if (roster.isSubscriptionPreApprovalSupported()) {
					roster.preApprove(newMasterAddress);
				}
				RosterUtil.askForSubscriptionIfRequired(roster, newMasterAddress);
			}
		});
	}

	Context getContext() {
		return mContext;
	}

	private void sendAsMessage(org.projectmaxs.shared.global.Message message,
			String originIssuerInfo, String originId) {
		if (!shouldUseXmppConnection()) {
			// TODO I think that this could for example happen when the service
			// is not started but e.g. the SMS receiver get's a new message.
			LOG.i("sendAsMessage: Not connected, adding message to DB. mConnection=" + mConnection);
			mMessagesTable.addMessage(message, Constants.ACTION_SEND_AS_MESSAGE, originIssuerInfo,
					originId);
			return;
		}

		Message packet = new Message();
		packet.setType(Message.Type.chat);
		packet.setBody(TransformMessageContent.toString(message));
		packet.setThread(originId);

		// Add a private carbon extension so that this message wont get carbon copied. MAXS does
		// already send the message to all resources. If a recipient has carbons enabled and we
		// wouldn't add the private element, then he would receive the message multiple times.
		CarbonExtension.Private.addTo(packet);

		// Add a MAXS element. MAXS itself will ignore messages with a MAXS element in order to
		// prevent endless loops of message sending between one or multiple MAXS instances.
		MAXSElement.addTo(packet);

		List<EntityJid> toList = new LinkedList<>();

		// No 'originIssueInfo (which is the to JID in this case) specified. The message is typical
		// a notification, so we are going to broadcast it to all master JIDs.
		if (originIssuerInfo == null) {
			Set<BareJid> jidsWithExcludedResources = new HashSet<BareJid>();
			Roster roster = Roster.getInstanceFor(mConnection);
			// Broadcast to all masterJID resources
			for (BareJid masterJid : mSettings.getMasterJids()) {
				Collection<Presence> presences = roster.getAvailablePresences(masterJid);
				for (Presence p : presences) {
					Jid jid = p.getFrom();
					EntityFullJid fullJID = jid.asEntityFullJidIfPossible();
					if (fullJID == null) {
						LOG.e("Could not convert '" + jid + "' to full JID");
						continue;
					}
					if (!mSettings.isExcludedResource(fullJID.getResourcepart())) {
						toList.add(fullJID);
					} else {
						jidsWithExcludedResources.add(fullJID.asBareJid());
					}
				}
			}

			// Broadcast to all offline masterJIDs
			for (EntityBareJid masterJid : mSettings.getMasterJids()) {
				boolean found = false;
				for (EntityJid toJid : toList) {
					if (toJid.asBareJid().equals(masterJid)) {
						found = true;
						break;
					}
				}
				// Maybe add this master JID, if it isn't already contained in toList
				if (!found) {
					if (jidsWithExcludedResources.contains(masterJid)
							&& roster.getPresences(masterJid).size() == 1) {
						// Do not send a message to this JID if it would get received by an excluded
						// resource, ie. when the excluded resource is the only online presence.
						continue;
					}
					toList.add(masterJid);
				}
			}
		}
		// A JID was specified as receiver. This are typical replies to a command send by the
		// receiver. This is not a notification, do not broadcast.
		else {
			EntityFullJid to;
			try {
				to = JidCreate.entityFullFrom(originIssuerInfo);
			} catch (XmppStringprepException e) {
				LOG.e("Could not convert originIssueInfo to full JID", e);
				return;
			}
			toList.add(to);
		}

		boolean atLeastOneSupportsXHTMLIM = false;
		for (EntityJid jid : toList) {
			if (!jid.hasResource()) {
				continue;
			}

			try {
				atLeastOneSupportsXHTMLIM = XHTMLManager.isServiceEnabled(mConnection, jid);
			} catch (Exception e) {
				atLeastOneSupportsXHTMLIM = false;
			}
			if (atLeastOneSupportsXHTMLIM) break;
		}
		if (atLeastOneSupportsXHTMLIM)
			XHTMLIMUtil.addXHTMLIM(packet, TransformMessageContent.toFormatedText(message));

		try {
			MultipleRecipientManager.send(mConnection, packet, toList, null, null);
		} catch (Exception e) {
			LOG.e("sendAsMessage: Got Exception, adding message to DB", e);
			mMessagesTable.addMessage(message, Constants.ACTION_SEND_AS_MESSAGE, originIssuerInfo,
					originId);
		}

		// Stop the current bundleAndDefer *after* the message has been sent.
		XMPPBundleAndDefer.stopCurrentBundleAndDefer();
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

		// Trim the command to remove extra whitespace, which e.g. could be send by clients trying
		// to negotiate OTR. References:
		// - https://github.com/python-otr/gajim-otr/issues/9
		// - https://trac-plugins.gajim.org/ticket/97
		command = command.trim();

		String issuerInfo = message.getFrom().toString();
		LOG.d("newMessageFromMasterJID: command=" + command + " from=" + issuerInfo);

		Intent intent = new Intent(GlobalConstants.ACTION_PERFORM_COMMAND);
		CommandOrigin origin = new CommandOrigin(Constants.PACKAGE,
				Constants.ACTION_SEND_AS_MESSAGE, issuerInfo, null);
		intent.putExtra(TransportConstants.EXTRA_COMMAND, command);
		intent.putExtra(TransportConstants.EXTRA_COMMAND_ORIGIN, origin);
		intent.setClassName(GlobalConstants.MAIN_PACKAGE,
				TransportConstants.MAIN_TRANSPORT_SERVICE);
		ComponentName cn = mContext.startService(intent);
		if (cn == null) {
			LOG.e("newMessageFromMasterJID: could not start main transport service");
		}
	}

	private void scheduleReconnect(String optionalReason) {
		if (mReconnectHandler == null) mReconnectHandler = new Handler();
		newState(State.WaitingForRetry, optionalReason);
		mReconnectHandler.removeCallbacks(mReconnectRunnable);
		int reconnectDelaySeconds;
		final int MINIMAL_DELAY_SECONDS = 10;
		final int ATTEMPTS_WITHOUT_PENALTY = 60;
		if (mReconnectionAttemptCount <= ATTEMPTS_WITHOUT_PENALTY) {
			reconnectDelaySeconds = MINIMAL_DELAY_SECONDS;
		} else {
			int delayFunctionResult = MINIMAL_DELAY_SECONDS * ((int) Math
					.pow(mReconnectionAttemptCount - ATTEMPTS_WITHOUT_PENALTY - 1, 1.2));
			// Maximum delay is 30 minutes
			reconnectDelaySeconds = Math.max(delayFunctionResult, 60 * 30);
		}
		mReconnectionAttemptCount++;
		LOG.d("scheduleReconnect: scheduling reconnect in " + reconnectDelaySeconds + " seconds");
		mReconnectHandler.postDelayed(mReconnectRunnable, reconnectDelaySeconds * 1000);
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
	 *            the optional reason for the new state
	 */
	private synchronized void newState(State newState, String reason) {
		if (reason == null) reason = "";
		synchronized (mStateChangeListeners) {
			mState = newState;
			switch (newState) {
			case Connected:
				for (StateChangeListener l : mStateChangeListeners) {
					try {
						l.connected(mConnection);
					} catch (NotConnectedException e) {
						LOG.w("newState", e);
						// Do not call 'changeState(State.Disconnected)' here, instead simply
						// schedule reconnect since we obviously didn't reach the connected state.
						// Changing the state to Disconnected will create a transition from
						// 'Connecting' to 'Disconnected', which why avoid implementing here
						scheduleReconnect("Disconnected while connecting");
						return;
					}
				}
				mConnected = true;
				break;
			case InstantDisconnected:
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
					l.waitingForRetry(reason);
				break;
			default:
				break;
			}
		}
	}

	private synchronized void changeState(State desiredState) {
		LOG.d("changeState: mState=" + mState + ", desiredState=" + desiredState);
		switch (mState) {
		case Connected:
			switch (desiredState) {
			case Connected:
				break;
			case Disconnected:
				disconnectConnection(false);
				break;
			case InstantDisconnected:
			case WaitingForNetwork:
				disconnectConnection(true);
				newState(desiredState);
				break;
			default:
				throw new IllegalStateException();
			}
			break;
		case InstantDisconnected:
		case Disconnected:
			switch (desiredState) {
			case InstantDisconnected:
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
			case InstantDisconnected:
			case Disconnected:
				newState(desiredState);
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
				// Do nothing here, instead, wait until the reconnect runnable did it's job.
				// Otherwise deadlocks may occur, because the connection attempts will block the
				// main thread, which will prevent SmackAndroid from receiving the
				// ConnecvitvityChange receiver and calling Resolver.refresh(). So we have no
				// up-to-date DNS server information, which will cause connect to fail.
				break;
			case InstantDisconnected:
			case Disconnected:
				newState(desiredState);
				mReconnectHandler.removeCallbacks(mReconnectRunnable);
				break;
			default:
				throw new IllegalStateException();
			}
			break;
		default:
			throw new IllegalStateException("changeState: Unknown state change combination. mState="
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

		LOG.d("tryToConnect: Changing state to 'Connecting'");
		newState(State.Connecting);

		XMPPTCPConnection connection;
		boolean newConnection = false;

		// We need to use an Application context instance here, because some Contexts may not work.
		XMPPTCPConnectionConfiguration latestConnectionConfiguration;
		try {
			latestConnectionConfiguration = mSettings.getConnectionConfiguration(mContext);
		} catch (XmppStringprepException e) {
			LOG.e("tryToConnect: getConnectionConfiguration failed. New State: Disconnected", e);
			newState(State.Disconnected, e.getLocalizedMessage());
			return;
		}
		if (mConnection == null || mConnectionConfiguration != latestConnectionConfiguration) {
			mConnectionConfiguration = latestConnectionConfiguration;
			connection = new XMPPTCPConnection(mConnectionConfiguration);

			final Roster roster = Roster.getInstanceFor(connection);

			// Setup the roster store
			File rosterStoreDirectory = FileUtil.getFileDir(mContext, "rosterStore");
			RosterStore rosterStore = DirectoryRosterStore.init(rosterStoreDirectory);
			roster.setRosterStore(rosterStore);

			roster.addRosterLoadedListener(new RosterLoadedListener() {
				@Override
				public void onRosterLoaded(Roster roster) {
					LOG.d("RosterLoadedListener.onRosterLoaded() invoked, roster has been loaded");
				}

				@Override
				public void onRosterLoadingFailed(Exception exception) {
					LOG.w("Failed to load roster", exception);
				}
			});

			newConnection = true;
		} else {
			connection = mConnection;
		}

		// Stream Management (XEP-198)
		connection.setUseStreamManagement(mSettings.isStreamManagementEnabled());
		// Again a value that's hard to get right. Right now, we try it with 5 minutes, as Stream
		// resumption is meant for situations where the network switches, not when the Android
		// system kills and later restarts the service (in which case, SM resumption would be not
		// possible anyways).
		connection.setPreferredResumptionTime(5 * 60); // 5 minutes

		// Disable bundle and defer so that the connection and login sequence is fast. :)
		XMPPBundleAndDefer.disableBundleAndDefer();

		LOG.d("tryToConnect: Calling connect() on XMPP connection");
		try {
			connection.connect();
		} catch (Exception e) {
			XMPPBundleAndDefer.enableBundleAndDefer();
			LOG.e("tryToConnect: Exception from connect()", e);
			if (e instanceof SmackException.EndpointConnectionException) {
				SmackException.EndpointConnectionException ce = (SmackException.EndpointConnectionException) e;
				String error = "The following host's failed to connect to:";
				for (RemoteConnectionException<?> rce : ce.getConnectionExceptions())
					error += " " + rce;
				LOG.d("tryToConnect: " + error);
			}
			scheduleReconnect(e.getLocalizedMessage());
			return;
		}

		LOG.d("tryToConnect: connect() returned without exception, calling login()");
		try {
			connection.login();
		} catch (NoResponseException e) {
			LOG.w("tryToConnect: NoResponseException. Scheduling reconnect.");
			scheduleReconnect("Not response while loggin in.");
			return;
		} catch (Exception e) {
			LOG.e("tryToConnect: login failed. New State: Disconnected", e);
			newState(State.Disconnected, e.getLocalizedMessage());
			return;
		} finally {
			XMPPBundleAndDefer.enableBundleAndDefer();
		}
		// Login Successful

		mConnection = connection;

		if (newConnection) {
			synchronized (mStateChangeListeners) {
				for (StateChangeListener l : mStateChangeListeners) {
					l.newConnection(mConnection);
				}
			}
		}

		mReconnectionAttemptCount = 0;
		newState(State.Connected);

		LOG.d("tryToConnect: successfully connected \\o/");
	}

	private synchronized void disconnectConnection(boolean instant) {
		if (mConnection != null) {
			if (mConnection.isConnected()) {
				newState(State.Disconnecting);
				LOG.d("disconnectConnection: disconnect start. instant=" + instant);
				if (instant) {
					mConnection.instantShutdown();
				} else {
					mConnection.disconnect();
				}
				LOG.d("disconnectConnection: disconnect stop");
			}
			newState(State.Disconnected);
		}
	}

	private boolean shouldUseXmppConnection() {
		if (mConnection == null) {
			return false;
		}
		if (!mConnection.isAuthenticated()) {
			return false;
		}
		// If it is not connected and not resumable, return false
		if (!mConnection.isConnected() && !mConnection.isDisconnectedButSmResumptionPossible()) {
			return false;
		}
		return true;
	}

}
