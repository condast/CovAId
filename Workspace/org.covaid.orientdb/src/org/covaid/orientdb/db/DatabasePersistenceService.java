package org.covaid.orientdb.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.covaid.orientdb.core.AbstractPersistenceService;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

/**
 * Handles the Orient Databae
 * @See :https://orientdb.com/docs/2.2/documenttx-Database-Tinkerpop.html
 * @author Kees
 *
 * @param <D>
 * @param <Descriptor>
 */
public class DatabasePersistenceService extends AbstractPersistenceService {
	
	protected static final String S_AIEONF = "AieonF";

	private OrientGraphFactory factory;

	private static DatabasePersistenceService service = new DatabasePersistenceService();
	
	private DatabasePersistenceService() {
		super( Types.GRAPH, S_AIEONF );
	}

	public static DatabasePersistenceService getInstance() {
		return service;
	}
	
	@Override
	public EntityManager getManager() {
		//if( this.factory == null )
		return null;
		//return//database.getnull;
	}

	/**
	 * Create a database
	 * @return
	 */
	public OrientGraph createDatabase() {
		if(!isConnected())
			return null;
		return factory.getTx(); 
	}

	/**
	 * Create a cluster for the given domain
	 * @param domain
	 */
	public void createCluster( String cluster ) {
		String source = super.getSource();
		factory = new OrientGraphFactory(source);
		OrientGraphNoTx graph = factory.getNoTx();
		ODatabaseDocumentTx database = graph.getRawGraph();
		if( !database.existsCluster( cluster ))
			database.addCluster( cluster );
	}

	@Override
	protected boolean onConnect() {
		String source = super.getSource();
		factory = new OrientGraphFactory(source);
		//factory = new OrientGraphFactory(source, user.getUserName(), String.valueOf(user.getToken() )).setupPool(1, 10);	
		return true;
	}

	@Override
	protected boolean onDisconnect() {

		boolean result = ( factory != null );
		if( result )
			factory.close();
		return result;
	}

	@Override
	public EntityManagerFactory getFactory() {
		return (EntityManagerFactory) factory;
	}
}