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

package org.projectmaxs.main.xmpp;

import java.util.HashSet;
import java.util.Set;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.projectmaxs.main.MAXSService;
import org.projectmaxs.main.Settings;
import org.projectmaxs.main.StateChangeListener;

import android.content.Context;

public class XMPPService {
	private Set<StateChangeListener> mStateChangeListeners = new HashSet<StateChangeListener>();
	private State mState = State.Disconnected;
	private Settings mSettings;
	private XMPPConnection mConnection;
	private ConnectionListener mConnectionListener;
	private PacketListener mChatPacketListener;
	private MAXSService.LocalService mMAXSLocalService;

	public XMPPService(Context ctx, MAXSService.LocalService maxsLocalService) {
		mSettings = Settings.getInstance(ctx);
		mMAXSLocalService = maxsLocalService;
	}

	public enum State {
		Connected, Connecting, Disconnecting, Disconnected, WaitingForNetwork, WaitingForRetry;
	}

	public void connect() {
		changeState(XMPPService.State.Connected);
	}

	public void disconnect() {
		changeState(XMPPService.State.Disconnected);
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

	private void newState(State newState) {
		for (StateChangeListener l : mStateChangeListeners)
			l.newState(newState);
		mState = newState;
	}

	private synchronized void changeState(State newState) {
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
		case Disconnected:
			switch (newState) {
			case Connected:
				tryToConnect();
				break;
			case Disconnected:
			case WaitingForNetwork:
				newState(newState);
				break;
			default:
				throw new IllegalStateException();
			}
		case WaitingForNetwork:
			switch (newState) {
			case Connected:
				tryToConnect();
				break;
			case WaitingForNetwork:
				newState(State.Connected);
				break;
			case Disconnected:
				newState(State.Disconnected);
				break;
			default:
				throw new IllegalStateException();
			}
		case WaitingForRetry:
			switch (newState) {
			case Connected:
				newState(State.Connected);
				break;
			case Disconnected:
			case WaitingForNetwork:
				newState(newState);
				break;
			default:
				throw new IllegalStateException();
			}
		default:
			throw new IllegalStateException();
		}
	}

	private void tryToConnect() {
		XMPPConnection con;
		boolean newConnection = false;
		if (mSettings.connectionSettingsObsolete() || mConnection == null) {
			try {
				con = createNewConnection(mSettings);
			} catch (Exception e) {
				// TODO maybeStartReconnect
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
			// TODO
			return;
		}

		if (!con.isAuthenticated()) {
			try {
				con.login(mSettings.login(), mSettings.password(), "MAXS");
			} catch (XMPPException e) {
				// TODO
			}
		}

		mConnection = con;

		// TODO maybe only create and add new listener if newConnection
		mConnectionListener = new ConnectionListener() {

			@Override
			public void connectionClosed() {
				// TODO Auto-generated method stub

			}

			@Override
			public void connectionClosedOnError(Exception arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void reconnectingIn(int arg0) {
				throw new IllegalStateException("Reconnection Manager is running");
			}

			@Override
			public void reconnectionFailed(Exception arg0) {
				throw new IllegalStateException("Reconnection Manager is running");
			}

			@Override
			public void reconnectionSuccessful() {
				throw new IllegalStateException("Reconnection Manager is running");
			}

		};
		mConnection.addConnectionListener(mConnectionListener);

		for (StateChangeListener l : mStateChangeListeners) {
			if (newConnection) l.newConnection(mConnection);
		}

		// TODO handle offline messages as StateChangeListener
		// TODO ping failed listener as StateChangeListener
		// TODO XHMTL disable as StateChangeListener

		mConnection.addPacketListener(new PacketListener() {

			@Override
			public void processPacket(Packet packet) {
				Message msg = (Message) packet;
				String from = msg.getFrom();

				if (mSettings.isMasterJID(from)) {
					mMAXSLocalService.performCommandFromMessage(msg);
				}
			}

		}, new MessageTypeFilter(Message.Type.chat));
		newState(State.Connected);
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

		return new XMPPConnection(conf);
	}

	private void disconnectConnection() {
		if (mConnection != null) {
			mConnection.removePacketListener(mChatPacketListener);
			mChatPacketListener = null;
			mConnection.removeConnectionListener(mConnectionListener);
			mConnectionListener = null;
			if (mConnection.isConnected()) {
				// TODO
				mConnection.disconnect();
			}
		}
	}
}
