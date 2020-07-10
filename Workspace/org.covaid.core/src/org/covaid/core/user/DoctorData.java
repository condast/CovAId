package org.covaid.core.user;

import org.condast.commons.authentication.user.ILoginUser;
import org.covaid.core.def.IUserData;

public interface DoctorData {

	enum RegistrationEvents{
		CONFIGURATION_ADDED,
		CREATE_VESSEL,
		REMOVE_VESSEL,
		VESSEL_SIGN_ON,
		VESSEL_SIGN_OFF,
		UNREGISTER_VESSEL;
	}

	ILoginUser getUser();
	
	IUserData getUserData();

	void addRegistrationListener(IUserRegistrationListener listener);

	void removeRegistrationListener(IUserRegistrationListener listener);

	/**
	 * update the user data
	 * @param userData
	 */
	public void update( IUserData userData );
	
	void dispose();

}