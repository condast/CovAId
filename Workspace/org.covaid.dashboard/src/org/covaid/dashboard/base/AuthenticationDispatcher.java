package org.covaid.dashboard.base;

import java.util.HashMap;
import java.util.Map;

import org.condast.commons.data.user.ILoginUser;
import org.condast.commons.jpa.authentication.core.ILoginProvider;
import org.covaid.core.def.ICovaidContext;

public class AuthenticationDispatcher{

	private static AuthenticationDispatcher dispatcher = new AuthenticationDispatcher();
	
	private volatile ILoginProvider loginProvider;

	private Map<Long, ILoginUser> users;
	
	private AuthenticationDispatcher() {
		super();
		this.users = new HashMap<>();
	}

	public static AuthenticationDispatcher getInstance() {
		return dispatcher;
	}

	public boolean hasUser(long token) {
		ILoginUser user = this.users.get(token);
		return ( user != null);
	}

	public void addUser(long token, ILoginUser user) {
		this.users.put(token, user);
	}

	public ILoginUser getUser(long token) {
		ILoginUser user = this.users.get(token);
		return user;
	}

	public ILoginUser remove(long token) {
		ILoginUser user = this.users.remove(token);
		this.loginProvider.logout( ICovaidContext.S_COVAID, user);
		return user;
	}
		

	public ILoginProvider getLoginProvider() {
		return loginProvider;
	}

	public void setLoginProvider(ILoginProvider loginProvider) {
		this.loginProvider = loginProvider;
	}

	public boolean logout(ILoginUser loginUser) {
		return this.loginProvider.logout(ICovaidContext.S_COVAID, loginUser);
	}
}