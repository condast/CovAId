package org.covaid.dashboard.service;

import java.util.logging.Logger;

import org.condast.commons.authentication.core.ILoginProvider;
import org.covaid.dashboard.authentication.AuthenticationDispatcher;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = AuthenticationComponent.COMPONENT_NAME
)
public class AuthenticationComponent{

	public static final String COMPONENT_NAME = "org.covaid.dashboard.service.login";

	private AuthenticationDispatcher dispatcher = AuthenticationDispatcher.getInstance();
	
    private static final Logger logger = Logger.getLogger( AuthenticationComponent.class.getName());
    
	@Activate
	public void activate(){
		logger.info("Activating the " + COMPONENT_NAME);		
	}

	@Deactivate
	public void deactivate(){
		logger.info("Deactivating the" + COMPONENT_NAME);				
	}

	@Reference( cardinality = ReferenceCardinality.AT_LEAST_ONE,
			policy=ReferencePolicy.DYNAMIC)
	public void setFactory( ILoginProvider factory ){
		dispatcher.setFactory(factory);
	}

	public void unsetFactory( ILoginProvider factory ){
		dispatcher.unsetFactory(factory);
	}
}
