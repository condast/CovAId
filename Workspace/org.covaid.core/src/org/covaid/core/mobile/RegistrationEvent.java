package org.covaid.core.mobile;

import java.util.EventObject;
import org.condast.commons.auth.AuthenticationData;
import org.covaid.core.def.IMobile;

public class RegistrationEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	private IMobileRegistration.RegistrationTypes type;
	private IMobile mobile;
	private AuthenticationData auth;
	
	public RegistrationEvent(Object source, IMobileRegistration.RegistrationTypes type, AuthenticationData auth, IMobile mobile ) {
		super(source);
		this.type = type;
		this.auth = auth;
		this.mobile = mobile;
	}

	protected IMobileRegistration.RegistrationTypes getType() {
		return type;
	}

	public AuthenticationData getAuthenticationData() {
		return auth;
	}

	protected IMobile getMobile() {
		return mobile;
	}
}
