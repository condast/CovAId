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
	public synchronized void bindEMF( IOrientEntityManagerFactory emf) {
		super.bindEMF(emf);
	}

	@Override
	public synchronized void unbindEMF( IOrientEntityManagerFactory emf) {
		super.unbindEMF(emf);
	}

	protected void onBindFactory( IOrientEntityManagerFactory factory) {
		service.setEMF(factory);
	}


	@Override
	protected void onUnbindFactory( IOrientEntityManagerFactory factory) {
		service.disconnect();
	}
}