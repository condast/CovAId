package org.covaid.core.user;

import java.util.EventObject;

import org.covaid.core.def.IUserData;

public class UserRegistrationEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private DoctorData.RegistrationEvents type;
	
	private IUserData userData;
	
	public UserRegistrationEvent(DoctorData source, DoctorData.RegistrationEvents type, IUserData userData ) {
		super(source);
		this.type = type;
		this.userData = userData;
	}
	
	public DoctorData getRegistrationManager() {
		return (DoctorData) super.getSource();
	}
	
	public DoctorData.RegistrationEvents getType() {
		return type;
	}

	public IUserData getUserData() {
		return userData;
	}
}
