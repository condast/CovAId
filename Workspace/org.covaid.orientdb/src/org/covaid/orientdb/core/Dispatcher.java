package org.covaid.orientdb.core;

import java.util.ArrayList;
import java.util.Collection;

import org.condast.commons.authentication.core.AuthenticationEvent;
import org.condast.commons.authentication.core.IAuthenticationListener;
import org.condast.commons.authentication.user.ILoginUser;


public class Dispatcher implements IAuthenticationListener{

	public static final String S_ERR_INVALID_CALL = "It is not allowed to have more than one secure call";
	
	private static Dispatcher service = new Dispatcher();

	private ILoginUser user;
	
	private Collection<IAuthenticationListener> listeners;

	private Dispatcher() {
		user = null;	
		listeners = new ArrayList<>();
	}

	public static Dispatcher getInstance(){
		return service;
	}

	public void addListener( IAuthenticationListener listener ) {
		this.listeners.add(listener);
	}

	public void removeListener( IAuthenticationListener listener ) {
		this.listeners.remove(listener);
	}
	
	protected void notifyListeners( AuthenticationEvent event ) {
		for( IAuthenticationListener listener: this.listeners )
			listener.notifyLoginChanged(event);
	}
	
	public boolean isRegistered(long id, long security) {
			return false;
		//return provider.isRegistered(id, name);
	}

	public boolean isLoggedIn( long userId, long security ) {
		return user.isCorrect(userId, security );
	}
	
	public ILoginUser getUser() {
		return user;
	}
	
	@Override
	public void notifyLoginChanged(AuthenticationEvent event) {
		switch( event.getEvent() ) {
		case LOGOUT:
			user = null;
			break;
		default:
			user = event.getUser();
		break;
		}
		notifyListeners(event);
	}
}
