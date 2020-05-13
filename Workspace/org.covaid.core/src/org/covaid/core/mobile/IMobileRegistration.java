package org.covaid.core.mobile;

public interface IMobileRegistration<T extends Object> {

	public enum RegistrationTypes{
		REGISTER,
		UNREGISTER;
	}
	
	public void notifyMobileRegistration( RegistrationEvent<T> event );
}
