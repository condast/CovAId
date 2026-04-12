package org.covaid.mobile.service;

import org.condast.commons.persistence.service.AbstractFactoryService;
import org.covaid.mobile.Activator;
import org.covaid.mobile.core.Dispatcher;
import org.covaid.orientdb.object.IOrientEntityManagerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Used by Gemini Blueprint
 * @author Kees
 *
 */
@Component( name="org.covaid.mobile.entity.service",
immediate=true)
public class EntityManagerService extends AbstractFactoryService<IOrientEntityManagerFactory>{

	private Dispatcher service = Dispatcher.getInstance();
	
	public EntityManagerService() {
		super( Activator.BUNDLE_ID );
	}
	
	
	@Reference( cardinality = ReferenceCardinality.MANDATORY,
	policy=ReferencePolicy.DYNAMIC)
	@Override
	public synchronized void bindFactory( IOrientEntityManagerFactory emf) {
		service.setEMF(emf);
		super.bindFactory(emf);
	}

	@Override
	public synchronized void unbindFactory( IOrientEntityManagerFactory emf) {
		service.disconnect();
		super.unbindFactory(emf);
	}


	@Override
	public void activate() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		
	}
}