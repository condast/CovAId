package org.covaid.core.user;

import java.util.EventObject;

import org.covaid.core.data.DoctorData;
import org.covaid.core.def.IUserData;
import org.covaid.core.doctor.IDoctorDataListener;

public class UserRegistrationEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private IDoctorDataListener.DocterDataEvents type;
	
	private IUserData userData;
	
	public UserRegistrationEvent(DoctorData source, IDoctorDataListener.DocterDataEvents type, IUserData userData ) {
		super(source);
		this.type = type;
		this.userData = userData;
	}
	
	public DoctorData getRegistrationManager() {
		return (DoctorData) super.getSource();
	}
	
	public IDoctorDataListener.DocterDataEvents getType() {
		return type;
	}

	public IUserData getUserData() {
		return userData;
	}
}
