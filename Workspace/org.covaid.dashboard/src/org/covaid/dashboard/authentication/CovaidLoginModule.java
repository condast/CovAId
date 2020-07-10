package org.covaid.dashboard.authentication;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import org.condast.commons.authentication.callback.LoginUserCallback;
import org.condast.commons.authentication.core.IAuthenticationManager;
import org.condast.commons.authentication.core.ILoginProvider;
import org.condast.commons.authentication.module.AbstractLoginModule;
import org.condast.commons.authentication.user.ILoginUser;
import org.covaid.core.user.DoctorData;
import org.covaid.dashboard.core.Dispatcher;

/*
*
* Copyright (c) 2000, 2002, Oracle and/or its affiliates. All rights reserved.
*
* Redistribution and use in source and binary forms, with or
* without modification, are permitted provided that the following
* conditions are met:
*
* -Redistributions of source code must retain the above copyright
* notice, this  list of conditions and the following disclaimer.
*
* -Redistribution in binary form must reproduct the above copyright
* notice, this list of conditions and the following disclaimer in
* the documentation and/or other materials provided with the
* distribution.
*
* Neither the name of Oracle nor the names of
* contributors may be used to endorse or promote products derived
* from this software without specific prior written permission.
*
* This software is provided "AS IS," without a warranty of any
* kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
* WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
* EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
* DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF  OR
* RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR
* ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE
* FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,
* SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
* CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF
* THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
*
* You acknowledge that Software is not designed, licensed or
* intended for use in the design, construction, operation or
* maintenance of any nuclear facility.
*/

import org.eclipse.swt.SWT;

/**
* <p> This sample LoginModule authenticates users with a password.
*
* <p> This LoginModule only recognizes one user:       testUser
* <p> testUser's password is:  testPassword
*
* <p> If testUser successfully authenticates itself,
* a <code>SamplePrincipal</code> with the testUser's user name
* is added to the Subject.
*
* <p> This LoginModule recognizes the debug option.
* If set to true in the login Configuration,
* debug messages will be output to the output stream, System.out.
*
*/
public class CovaidLoginModule extends AbstractLoginModule {

	//Same as in plugin.xml
	private static final String S_ARNAC_MODULE = "ArnacLoginModule";

	private static final String S_HANDLER_FOUND = "HANDLER FOUND: ";
	private static final String S_COMMITING = "COMMITTING: ";
	private static final String S_LOGIN_SUCCESSFUL = "LOGIN SUCCESSFUL: ";
	private static final String S_ERR_NO_MODULE_LOADED = "COULD NOT LOAD MODULE AS A DECLARATIVE SERVICE!!!";

	private IAuthenticationManager<DoctorData> manager = AuthenticationManager.getInstance();
	private LoginUserCallback lcb;
	
	private Logger logger = Logger.getLogger( this.getClass().getName());
	
 	public CovaidLoginModule() {
		super(S_ARNAC_MODULE);
	}

	public void initialise( ) {
		Map<String,?> state = new HashMap<>();
		Map<String,?> options = new HashMap<>();
		super.initialize(new Subject(), getHandlerIfNull(), state, options);
	}

	@Override
	public Collection<Callback> createCallbacks( CallbackHandler handler ) {
		Collection<Callback> callbacks = super.createCallbacks( handler );
		lcb = new LoginUserCallback();
		callbacks.add( lcb );		
		return callbacks;
	}

	@Override
	protected CallbackHandler getHandlerIfNull() {
		ILoginProvider provider = AuthenticationDispatcher.getInstance();
		if( provider == null ) {
			logger.warning(S_ERR_NO_MODULE_LOADED);
		}else {
			logger.info( S_HANDLER_FOUND + provider.getClass().getName() );		
		}
		return provider.createCallbackHandler();
	}

	@Override
	protected boolean verifyCredentials(String username, char[] password) {
		ILoginUser user = lcb.getUser();
		return ( user != null );
	}

	@Override
	public boolean commit() throws LoginException {
		logger.info( S_COMMITING);
		Subject subject = super.getSubject();
		Dispatcher dispatcher = Dispatcher.getInstance();
		DoctorData registration = null;//dispatcher.createRegistrationManager(lcb.getUser());
		manager.setData(registration);
		logger.info( S_LOGIN_SUCCESSFUL + ( registration != null ));
		//subject.getPublicCredentials().add(USERS);
		//subject.getPrivateCredentials().add(Display.getCurrent());
		subject.getPrivateCredentials().add(SWT.getPlatform());
		return super.commit();
	}

    @Override
	public boolean abort() throws LoginException {
         return true;
    }

    @Override
	public boolean logout() throws LoginException {
    	AuthenticationDispatcher.getInstance().logoutRequest();
    	return true;
    }
}