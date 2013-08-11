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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.projectmaxs.main.database.TransportRegistryTable;
import org.projectmaxs.shared.maintransport.TransportInformation;

import android.content.Context;

public class TransportRegistry {

	private static TransportRegistry sTransportRegistry;

	public static synchronized TransportRegistry getInstance(Context context) {
		if (sTransportRegistry == null) sTransportRegistry = new TransportRegistry(context);
		return sTransportRegistry;
	}

	private final Map<String, TransportInformation> mPackageTransport = new HashMap<String, TransportInformation>();

	private final Map<String, String> mPackageStatus = new HashMap<String, String>();

	private final Set<ChangeListener> mChangeListeners = new HashSet<ChangeListener>();

	private Context mContext;
	private TransportRegistryTable mTransportRegistryTable;

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
		return new ArrayList<TransportInformation>(Collections.unmodifiableCollection(mPackageTransport.values()));
	}

	public synchronized void updateStatus(String transportPackage, String status) {
		mPackageStatus.put(transportPackage, status);
		for (ChangeListener l : mChangeListeners)
			l.transportStatusChanged(transportPackage, status);
	}

	public synchronized void unregisterTransport(String transportPackage) {
		if (!mTransportRegistryTable.containsTransport(transportPackage)) return;
		remove(transportPackage);
		mTransportRegistryTable.deleteTransportInformation(transportPackage);
		for (ChangeListener l : mChangeListeners)
			l.transportUnregisted(transportPackage);
	}

	protected synchronized void registerTransport(TransportInformation transportInformation) {
		// first remove all traces of the Transport
		remove(transportInformation.getTransportPackage());
		add(transportInformation);
		mTransportRegistryTable.insertOrReplace(transportInformation);
		for (ChangeListener l : mChangeListeners)
			l.transportRegistered(transportInformation.getTransportPackage());
	}

	private void add(TransportInformation transportInformation) {

	}

	private void remove(String transportPackage) {

	}

	public interface ChangeListener {
		public void transportUnregisted(String transportPackage);

		public void transportRegistered(String transportPackge);

		public void transportStatusChanged(String transportPackage, String status);
	}
}
