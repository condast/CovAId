package org.covaid.rest.service;

import org.condast.commons.persistence.service.FactoryService;
import org.covaid.orientdb.object.IOrientEntityManagerFactory;
import org.covaid.rest.Activator;
import org.covaid.rest.core.Dispatcher;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Used by Gemini Blueprint
 * @author Kees
 *
 */
@Component( name="org.covaid.rest.entity.service",
immediate=true)
public class EntityManagerService extends FactoryService<IOrientEntityManagerFactory>{

	private Dispatcher service = Dispatcher.getInstance();
	
	public EntityManagerService() {
		super( Activator.BUNDLE_ID );
	}
	
	
	@Reference( cardinality = ReferenceCardinality.MANDATORY,
	policy=ReferencePolicy.DYNAMIC)
	@Override
	public synchronized void bindEMF( IOrientEntityManagerFactory emf) {
		service.setEMF(emf);
		super.bindEMF(emf);
	}

	@Override
	public synchronized void unbindEMF( IOrientEntityManagerFactory emf) {
		service.disconnect();
		super.unbindEMF(emf);
	}
}