package org.covaid.dashboard.service;

import org.condast.commons.ui.location.ILocationChangeListener;
import org.covaid.dashboard.core.LocationDispatcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component( name="org.covaid.dashboard.service.location",
			immediate=true)
public class LocationService {

	private LocationDispatcher dispatcher = LocationDispatcher.getInstance();
	
	@Reference( cardinality = ReferenceCardinality.AT_LEAST_ONE,
			policy=ReferencePolicy.DYNAMIC)
	public void bind( ILocationChangeListener listener){
		this.dispatcher.addLocationListener( listener );
	}

	public void unbind( ILocationChangeListener listener){
		this.dispatcher.removeLocationListener(listener);
	}
}
