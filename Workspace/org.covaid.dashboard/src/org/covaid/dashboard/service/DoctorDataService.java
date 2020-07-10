package org.covaid.dashboard.service;

import org.covaid.core.doctor.IDoctorDataProvider;
import org.covaid.dashboard.core.Dispatcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component( name="org.covaid.dashboard.service.doctor.data",
			immediate=true)
public class DoctorDataService {

	private Dispatcher dispatcher = Dispatcher.getInstance();
	
	@Reference( cardinality = ReferenceCardinality.AT_LEAST_ONE,
			policy=ReferencePolicy.DYNAMIC)
	public void bind( IDoctorDataProvider provider){
		this.dispatcher.addProvider( provider );
	}

	public void unbind( IDoctorDataProvider provider){
		this.dispatcher.removeProvider(provider);
	}
}
