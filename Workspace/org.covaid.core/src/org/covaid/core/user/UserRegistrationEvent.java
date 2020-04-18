package org.covaid.core.user;

import java.util.EventObject;

import org.covaid.core.def.IUserData;

public class UserRegistrationEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private IUserRegistration.RegistrationEvents type;
	
	private IUserData userData;
	
	public UserRegistrationEvent(IUserRegistration source, IUserRegistration.RegistrationEvents type, IUserData userData ) {
		super(source);
		this.type = type;
		this.userData = userData;
	}
	
	public IUserRegistration getRegistrationManager() {
		return (IUserRegistration) super.getSource();
	}
	
	public IUserRegistration.RegistrationEvents getType() {
		return type;
	}

	public IUserData getUserData() {
		return userData;
	}
}
