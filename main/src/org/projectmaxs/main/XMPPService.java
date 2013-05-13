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
import java.util.Set;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.projectmaxs.main.xmpp.HandleChatPacketListener;
import org.projectmaxs.main.xmpp.HandleConnectionListener;
import org.projectmaxs.shared.util.Log;

public class XMPPService {
	private Set<StateChangeListener> mStateChangeListeners = new HashSet<StateChangeListener>();
	private State mState = State.Disconnected;
	private Settings mSettings;
	private XMPPConnection mConnection;
	private MAXSService mMAXSLocalService;

	public XMPPService(MAXSService maxsLocalService) {
		mSettings = Settings.getInstance(maxsLocalService);
		mMAXSLocalService = maxsLocalService;

		addListener(new HandleChatPacketListener(mMAXSLocalService, mSettings));
		addListener(new HandleConnectionListener(mMAXSLocalService));
	}

	public enum State {
		Connected, Connecting, Disconnecting, Disconnected, WaitingForNetwork, WaitingForRetry;
	}

	public State getCurrentState() {
		return mState;
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
			// throw new IllegalStateException();
		}
	}

	private void tryToConnect() {
		Log.d("XMPPService.tryToConnect()");
		newState(State.Connecting);

		XMPPConnection con;
		boolean newConnection = false;
		if (mSettings.connectionSettingsObsolete() || mConnection == null) {
			try {
				con = createNewConnection(mSettings);
			} catch (Exception e) {
				Log.e("Exception from createNewConnection()", e);
				return;
			}
			mSettings.resetConnectionSettingsObsolete();
			newConnection = true;
		}
		else {
			con = mConnection;
		}

		try {
			con.connect();
		} catch (XMPPException e) {
			Log.e("Exception from connect()", e);
			return;
		}

		if (!con.isAuthenticated()) {
			try {
				con.login(mSettings.login(), mSettings.password(), "MAXS");
			} catch (XMPPException e) {
				Log.e("tryToConnect() login failed", e);
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

	private static XMPPConnection createNewConnection(Settings settings) throws XMPPException {
		ConnectionConfiguration conf;

		if (settings.manualServerSettings()) {
			conf = new ConnectionConfiguration(settings.serverHost(), settings.serverPort(), settings.serviceName());
		}
		else {
			conf = new AndroidConnectionConfiguration(settings.serviceName());
		}

		// conf.setSocketFactory(new SocketFactory() {
		//
		// @Override
		// public Socket createSocket(String host, int port) throws IOException,
		// UnknownHostException {
		// // TODO Auto-generated method stub
		// return null;
		// }
		//
		// @Override
		// public Socket createSocket(InetAddress host, int port) throws
		// IOException {
		// // TODO Auto-generated method stub
		// return null;
		// }
		//
		// @Override
		// public Socket createSocket(String host, int port, InetAddress
		// localHost, int localPort) throws IOException,
		// UnknownHostException {
		// // TODO Auto-generated method stub
		// return null;
		// }
		//
		// @Override
		// public Socket createSocket(InetAddress address, int port, InetAddress
		// localAddress, int localPort)
		// throws IOException {
		// // TODO Auto-generated method stub
		// return null;
		// }
		//
		// });

		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
		// {
		// conf.setTruststoreType("AndroidCAStore");
		// conf.setTruststorePassword(null);
		// conf.setTruststorePath(null);
		// } else {
		// conf.setTruststoreType("BKS");
		// String path = System.getProperty("javax.net.ssl.trustStore");
		// if (path == null) {
		// path = System.getProperty("java.home") + File.separator + "etc"
		// + File.separator + "security" + File.separator
		// + "cacerts.bks";
		// }
		// conf.setTruststorePath(path);
		// }

		conf.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		conf.setCompressionEnabled(false);

		conf.setSendPresence(false);

		return new XMPPConnection(conf);
	}

	private void disconnectConnection() {
		if (mConnection != null) {
			if (mConnection.isConnected()) {
				newState(State.Disconnecting);
				// TODO better diconnect handle (e.g. in extra thread)
				mConnection.disconnect();
			}
		}
	}
}
