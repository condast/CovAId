package org.covaid.mobile.service;

import javax.persistence.EntityManagerFactory;

import org.condast.commons.persistence.service.AbstractEntityManagerService;
import org.covaid.mobile.Activator;
import org.covaid.mobile.core.Dispatcher;

/**
 * Used by Gemini Blueprint
 * @author Kees
 *
 */
public class EntityManagerService extends AbstractEntityManagerService{

	private Dispatcher service = Dispatcher.getInstance();
	
	public EntityManagerService() {
		super( Activator.BUNDLE_ID );
	}
		
	@Override
	protected void onBindFactory(EntityManagerFactory factory) {
		service.setEMF(factory);
	}


	@Override
	protected void onUnbindFactory(EntityManagerFactory factory) {
		service.disconnect();
	}
}