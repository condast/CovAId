package org.covaid.dashboard.authentication;

import org.condast.commons.authentication.core.AbstractLoginClient;

public class AuthenticationDispatcher extends AbstractLoginClient{

	private static AuthenticationDispatcher dispatcher = new AuthenticationDispatcher();
	
	public static AuthenticationDispatcher getInstance(){
		return dispatcher;
	}
}