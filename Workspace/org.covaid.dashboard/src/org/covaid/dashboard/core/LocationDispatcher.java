package org.covaid.dashboard.core;

import java.util.ArrayList;
import java.util.Collection;

import org.condast.commons.ui.location.ILocationChangeListener;

public class LocationDispatcher {

	private static LocationDispatcher service = new LocationDispatcher();

	private Collection<ILocationChangeListener> listeners;

	private LocationDispatcher() {
		listeners = new ArrayList<>();
	}

	public static LocationDispatcher getInstance() {
		return service;
	}
	
	/**
	 * Used to give the simulators a location, which is usually the
	 * start coordinate of the selected field
	 * @param listener
	 */
	public void addLocationListener( ILocationChangeListener listener ) {
		this.listeners.add( listener );
	}

	public void removeLocationListener( ILocationChangeListener listener ) {
		this.listeners.remove( listener );
	}


}
