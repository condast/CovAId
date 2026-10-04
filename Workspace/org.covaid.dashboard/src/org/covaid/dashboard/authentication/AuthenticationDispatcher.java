package org.covaid.dashboard.authentication;

import org.condast.commons.jpa.authentication.core.AbstractLoginClient;

public class AuthenticationDispatcher extends AbstractLoginClient{

	private static AuthenticationDispatcher dispatcher = new AuthenticationDispatcher();
	
	public static AuthenticationDispatcher getInstance(){
		return dispatcher;
	}
}