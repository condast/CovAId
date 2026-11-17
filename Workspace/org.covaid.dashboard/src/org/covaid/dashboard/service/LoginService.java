package org.covaid.dashboard.service;

import java.util.logging.Logger;

import org.condast.commons.jpa.authentication.core.ILoginProvider;
import org.covaid.dashboard.base.AuthenticationDispatcher;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(service = LoginService.class,
immediate=true)
public class LoginService{

	@Reference(
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.DYNAMIC
			)
	private volatile ILoginProvider loginProvider;

	private AuthenticationDispatcher dispatcher = AuthenticationDispatcher.getInstance(); 

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	public LoginService() {
		super();
	}

	@Activate
	protected void activate() {
	    logger.info("LoginProvider available: " + (loginProvider != null));
	    dispatcher.setLoginProvider(loginProvider);
	}
}