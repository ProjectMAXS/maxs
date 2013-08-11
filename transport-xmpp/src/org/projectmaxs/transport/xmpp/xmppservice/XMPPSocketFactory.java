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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public class XMPPSocketFactory extends SocketFactory {
	private static SocketFactory sDefaultFactory = SocketFactory.getDefault();
	private static XMPPSocketFactory sInstance;

	private Socket socket;

	public static XMPPSocketFactory getInstance() {
		if (sInstance == null) sInstance = new XMPPSocketFactory();
		return sInstance;
	}

	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException {
		socket = sDefaultFactory.createSocket(arg0, arg1);
		setSockOpt(socket);
		return socket;
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		socket = sDefaultFactory.createSocket(host, port);
		setSockOpt(socket);
		return socket;
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException,
			UnknownHostException {
		socket = sDefaultFactory.createSocket(host, port, localHost, localPort);
		setSockOpt(socket);
		return socket;
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
			throws IOException {
		socket = sDefaultFactory.createSocket(address, port, localAddress, localPort);
		setSockOpt(socket);
		return socket;
	}

	private static void setSockOpt(Socket socket) throws IOException {
		socket.setKeepAlive(false);
		// Set socket timeout to2 hours, should be more then the ping interval
		// to avoid Exceptions on read()
		socket.setSoTimeout(120 * 60 * 1000);
		socket.setTcpNoDelay(false);
	}
}