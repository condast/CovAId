package org.covaid.orientdb.service;

import org.condast.commons.authentication.core.ILoginProvider;
import org.covaid.orientdb.core.Dispatcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class LoginConsumer {

	private Dispatcher dispatcher = Dispatcher.getInstance();
	private ILoginProvider provider;
	
	public LoginConsumer() {}

	@Reference
	public synchronized void setProvider(ILoginProvider provider) {
		this.provider = provider;
		this.provider.addAuthenticationListener(dispatcher);
	}

	public synchronized void unsetProvider(ILoginProvider prvider) {
		this.provider.removeAuthenticationListener(dispatcher);
		provider = null;
	}
}
