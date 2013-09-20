/*
    This file is part of Project MAXS.

    MAXS and its Transports is free software: you can redistribute it and/or modify
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.projectmaxs.main.database.TransportRegistryTable;
import org.projectmaxs.shared.global.util.Log;
import org.projectmaxs.shared.maintransport.TransportInformation;

import android.content.Context;

public class TransportRegistry {

	private static final Log LOG = Log.getLog();

	private static TransportRegistry sTransportRegistry;

	public static synchronized TransportRegistry getInstance(Context context) {
		if (sTransportRegistry == null) sTransportRegistry = new TransportRegistry(context);
		return sTransportRegistry;
	}

	private final List<TransportInformation> mTransportList = new ArrayList<TransportInformation>(5);
	private final Map<String, String> mPackageStatus = new ConcurrentHashMap<String, String>();

	private final Set<ChangeListener> mChangeListeners = Collections
			.newSetFromMap(new ConcurrentHashMap<ChangeListener, Boolean>());

	private final Context mContext;
	private final TransportRegistryTable mTransportRegistryTable;

	/**
	 * Constructor for TransportRegistry. Loads the TransportInformation from
	 * database into memory.
	 * 
	 * This constructor is synchronized guarded by getInstance().
	 * 
	 * @param context
	 */
	private TransportRegistry(Context context) {
		mContext = context;
		mTransportRegistryTable = TransportRegistryTable.getInstance(context);

		// Load the Transport information from the database
		Iterator<TransportInformation> it = mTransportRegistryTable.getAll().iterator();
		while (it.hasNext())
			add(it.next());
	}

	public synchronized void addChangeListener(ChangeListener listener) {
		mChangeListeners.add(listener);
	}

	public synchronized boolean removeChangeListener(ChangeListener listener) {
		return mChangeListeners.remove(listener);
	}

	public synchronized List<TransportInformation> getAllTransports() {
		return Collections.unmodifiableList(mTransportList);
	}

	public synchronized List<TransportInformation> getCopyAddListener(ChangeListener listener) {
		addChangeListener(listener);
		return new ArrayList(mTransportList);
	}

	public void updateStatus(String transportPackage, String status) {
		mPackageStatus.put(transportPackage, status);
		for (ChangeListener l : mChangeListeners)
			l.transportStatusChanged(transportPackage, status);
	}

	public String getStatus(String transportPackage) {
		return mPackageStatus.get(transportPackage);
	}

	public synchronized boolean containsTransport(String transportPackage) {
		for (TransportInformation ti : mTransportList)
			if (ti.getTransportPackage().equals(transportPackage)) return true;
		return false;
	}

	public synchronized void unregisterTransport(String transportPackage) {
		if (!mTransportRegistryTable.containsTransport(transportPackage)) return;
		remove(transportPackage);
	}

	protected synchronized void registerTransport(TransportInformation transportInformation) {
		// first remove all traces of the Transport
		remove(transportInformation.getTransportPackage());
		add(transportInformation);

	}

	private void add(TransportInformation transportInformation) {
		final String transportPackage = transportInformation.getTransportPackage();
		if (containsTransport(transportPackage)) {
			LOG.e("add: trying to add transport " + transportPackage + " thats already there");
			return;
		}
		mTransportList.add(transportInformation);
		Collections.sort(mTransportList);
		mPackageStatus.put(transportPackage, "unkown");
		mTransportRegistryTable.insertOrReplace(transportInformation);
		for (ChangeListener l : mChangeListeners)
			l.transportRegistered(transportInformation);
	}

	private void remove(String transportPackage) {
		if (!containsTransport(transportPackage)) {
			LOG.w("remove: transportInformation not found package=" + transportPackage);
			return;
		}
		updateStatus(transportPackage, "removed");
		Iterator<TransportInformation> it = mTransportList.iterator();
		TransportInformation ti = null;
		while (it.hasNext()) {
			TransportInformation cur = it.next();
			if (transportPackage.equals(cur.getTransportPackage())) {
				ti = cur;
				it.remove();
			}
		}
		mPackageStatus.remove(transportPackage);
		mTransportRegistryTable.deleteTransportInformation(transportPackage);
		for (ChangeListener l : mChangeListeners)
			l.transportUnregistered(ti);
	}

	public static abstract class ChangeListener {
		public void transportUnregistered(TransportInformation transportInformation) {};

		public void transportRegistered(TransportInformation transportInformation) {};

		public void transportStatusChanged(String transportPackage, String status) {};
	}
}
