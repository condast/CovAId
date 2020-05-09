package org.covaid.core.mobile;

public interface IMobileRegistration {

	public enum RegistrationTypes{
		REGISTER,
		UNREGISTER;
	}
	
	public void notifyMobileRegistration( RegistrationEvent event );
}
