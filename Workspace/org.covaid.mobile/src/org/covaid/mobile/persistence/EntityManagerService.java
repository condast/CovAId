package org.covaid.mobile.persistence;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;

import org.condast.commons.ICredentialsProvider;
import org.condast.commons.jpa.persistence.AbstractEntityManagerFactoryService;
import org.covaid.core.def.ICovaidContext;
import org.covaid.mobile.core.Dispatcher;
import org.covaid.orientdb.object.IOrientEntityManagerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Create the entity manager factory
 * @author Kees
 *
 */
@Component(immediate = true)
public class EntityManagerService extends AbstractEntityManagerFactoryService{

	public static final String BUNDLE_ID = "com.condast.authentication";

	Dispatcher service = Dispatcher.getInstance();

	@Reference(
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.DYNAMIC
			)
	private volatile ICredentialsProvider credentialsProvider;

	private Logger logger = Logger.getLogger( this.getClass().getName());

	public EntityManagerService() {
		super( BUNDLE_ID );
	}

	@Activate
	public void activate( BundleContext context ) throws Exception {
		try {
			super.activate( context );
			EntityManagerFactory emf = super.getFactory();
			logger.info("Persistence unit: " +
				    emf.getProperties().get(
				        "eclipselink.persistence-unit.name"));

				logger.info("Number of entities: " +
				    emf.getMetamodel().getEntities().size());

				emf.getMetamodel().getEntities().forEach(entity ->
				    logger.info("Entity: " +
				        entity.getJavaType().getName()));
			service.setEMF((IOrientEntityManagerFactory) emf);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void augmentProperties(Properties jdbcProperties) {
		try {
			this.credentialsProvider.augmentProperties(super.getPersistenceUnitName(), ICovaidContext.S_COVAID, jdbcProperties);

			logger.fine("JDBC URL: " +
					jdbcProperties.getProperty("javax.persistence.jdbc.url"));

			logger.fine("JDBC user: " +
					jdbcProperties.getProperty("javax.persistence.jdbc.user"));

			String password = jdbcProperties.getProperty(
					"javax.persistence.jdbc.password");

			logger.fine("Password resolved: " +
					(password != null
					&& !password.isBlank()
					&& !password.contains("${")));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Deactivate
	public synchronized void close() {
		super.close();
		service.disconnect();
	}
}