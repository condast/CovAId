package org.covaid.core.mobile;

import java.util.EventObject;
import org.condast.js.commons.utils.AuthenticationData;
import org.covaid.core.def.IMobile;

public class RegistrationEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;

	private IMobileRegistration.RegistrationTypes type;
	private IMobile<T> mobile;
	private AuthenticationData auth;
	
	public RegistrationEvent(Object source, IMobileRegistration.RegistrationTypes type, AuthenticationData auth, IMobile<T> mobile ) {
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

	protected IMobile<T> getMobile() {
		return mobile;
	}
}
