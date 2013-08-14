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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5BytestreamManager;
import org.jivesoftware.smackx.bytestreams.socks5.Socks5Proxy;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.projectmaxs.shared.global.GlobalConstants;
import org.projectmaxs.shared.global.aidl.IMAXSIncomingFileTransferService;
import org.projectmaxs.shared.global.util.AsyncServiceTask;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.global.util.SharedStringUtil;
import org.projectmaxs.transport.xmpp.Settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public class XMPPFileTransfer extends StateChangeListener implements FileTransferListener {

	private static final Log LOG = Log.getLog();

	private static Connection sConnection;

	private final Settings mSettings;
	private final Context mContext;
	private final WifiManager mWifiManager;
	private final Socks5Proxy mProxy;

	private FileTransferManager mFileTransferManager;

	private BroadcastReceiver mWifiBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)
					&& intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
				onWifiConnected();
			}
		}
	};

	protected XMPPFileTransfer(Context context) {
		mSettings = Settings.getInstance(context);
		mContext = context;
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mProxy = Socks5Proxy.getSocks5Proxy();
	}

	@Override
	public void fileTransferRequest(FileTransferRequest request) {
		String requestor = request.getRequestor();
		if (!mSettings.isMasterJID(requestor)) {
			LOG.w("File transfer from non master jid " + requestor);
			request.reject();
			return;
		}

		final String filename = request.getFileName();
		final String description = request.getDescription();
		final long size = request.getFileSize();
		InputStream isTmp;
		try {
			isTmp = request.accept().recieveFile();
		} catch (XMPPException e2) {
			LOG.e("fileTransferRequest", e2);
			return;
		}
		final InputStream is = isTmp;

		// final String content = "foobartest\ntest\n123\n";
		// final String filename = "foo";
		// final String description = "more foobar";
		// final InputStream is = new StringBufferInputStream(content);
		// final long size = content.length();

		new AsyncServiceTask<IMAXSIncomingFileTransferService>(
				new Intent(GlobalConstants.ACTION_INCOMING_FILETRANSFER), mContext) {

			@Override
			public IMAXSIncomingFileTransferService asInterface(IBinder iBinder) {
				return IMAXSIncomingFileTransferService.Stub.asInterface(iBinder);
			}

			@Override
			public void performTask(IMAXSIncomingFileTransferService iinterface) {
				try {
					ParcelFileDescriptor pfd = iinterface.incomingFileTransfer(filename, size, description);
					if (pfd == null) {
						LOG.e("fileTranferRequest: PFD from incomingFileTransfer is null");
						return;
					}
					OutputStream os = new ParcelFileDescriptor.AutoCloseOutputStream(pfd);

					int len;
					byte[] buf = new byte[1024];

					try {
						while ((len = is.read(buf)) > 0) {
							os.write(buf, 0, len);
						}
					} catch (IOException e) {
						LOG.e("fleTransferRequest", e);
					}
					finally {
						try {
							is.close();
						} catch (IOException e1) {
							LOG.e("fleTransferRequest", e1);
						}
						try {
							os.close();
						} catch (IOException e1) {
							LOG.e("fleTransferRequest", e1);
						}
					}
				} catch (RemoteException e) {
					LOG.e("fileTransferRequest", e);
				}
			}

		}.go();
	}

	@Override
	public void newConnection(Connection connection) {
		// disable streamhost prioritization
		Socks5BytestreamManager s5bsm = Socks5BytestreamManager.getBytestreamManager(connection);
		s5bsm.setProxyPrioritizationEnabled(false);
	}

	@Override
	public void connected(Connection connection) {
		sConnection = connection;
		mFileTransferManager = new FileTransferManager(connection);
		mFileTransferManager.addFileTransferListener(this);
		mContext.registerReceiver(mWifiBroadcastReceiver, new IntentFilter(
				WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION));
	}

	@Override
	public void disconnected(Connection connection) {
		sConnection = null;
		mFileTransferManager.removeFileTransferListener(this);
		mFileTransferManager = null;
		mContext.unregisterReceiver(mWifiBroadcastReceiver);
	}

	private void onWifiConnected() {
		WifiInfo info = mWifiManager.getConnectionInfo();
		List<String> addresses = new ArrayList<String>();

		if (info != null) {
			// There is an active Wifi connection
			String ip = SharedStringUtil.ipIntToString(info.getIpAddress());
			// Sometimes "0.0.0.0" gets returned
			if (!ip.equals("0.0.0.0")) addresses.add(ip);

		}
		// set an ip in case there is a Wifi Connection
		// otherwise addresses will be empty and local S5B proxy
		// will not be used
		mProxy.replaceLocalAddresses(addresses);
	}
}
