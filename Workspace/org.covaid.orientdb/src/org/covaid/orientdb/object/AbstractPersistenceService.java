package org.covaid.orientdb.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.condast.commons.Utils;
import org.condast.commons.persistence.service.IPersistenceService;
import org.condast.commons.service.IPersistencyServiceListener;
import org.condast.commons.service.ServiceConnectionException;

import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import org.condast.commons.service.IPersistencyServiceListener.Services;
import org.covaid.orientdb.def.IOrientPersistenceService;
import org.condast.commons.service.PersistencyServiceEvent;

/**
 * This utility class is provided with the example to execute the JDBC code that is 
 * required as part of the example.  It provides a main method that can be used to run the code
 * outside of OSGi and several methods that actually populate the DB.
 * 
 * @author keesp
 *
 */
public abstract class AbstractPersistenceService implements IOrientPersistenceService{

	private IOrientEntityManagerFactory factory;
	private OEntityManager manager;

	private String id;
	private String name;
	private boolean connected;
	private Map<String, String> args;

	private List<IPersistencyServiceListener> listeners;

	private Lock lock;
	
	private final Logger logger = Logger.getLogger( this.getClass().getCanonicalName());

	protected AbstractPersistenceService( String id, String name ) {
		this.id = id;
		this.name = name;
		this.connected = false;
		lock = new ReentrantLock();
		listeners = new ArrayList<IPersistencyServiceListener>();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * returns true if the service is connected
	 * @return
	 */
	@Override
	public boolean isEnabled(){
		return ( factory != null );  
	}

	@Override
	public OEntityManager getManager(){
		return manager;
	}

	public boolean open() {
		OObjectDatabaseTx db = this.getDatabase();
		if( db == null )
			return false;
		db.open( args.get(IOrientPersistenceService.Attributes.USER.toJdbcProperty()), 
				args.get( IOrientPersistenceService.Attributes.PASSWORD.toJdbcProperty()));
		return true;
	}

	public void close() {
		OObjectDatabaseTx db = getDatabase();
		if( db != null )
			db.close();
	}

	public boolean registerEntityClass( Class<?> clss) {
		OObjectDatabaseTx db = getDatabase();
		if( db == null)
			return false;
		db.getEntityManager().registerEntityClass(clss);
		return true;
	}
	
	@Override
	public OObjectDatabaseTx getDatabase() {
		return (this.factory == null )?null: this.factory.getDatabase();
	}
	
	@Override
	public synchronized void setEMF( IOrientEntityManagerFactory factory) {
		logger.info("Manager loaded: " + this.name + ": " + ( factory != null ) + "\n\n");
		this.factory = factory;
		this.notifyListeners( Services.ADD );
	}

	@Override
	public Map<String, String> getArgs() {
		return args;
	}

	protected abstract Map<String, String> onPrepareManager();

	protected abstract void onManagerCreated( OEntityManager manager );

	@Override
	public synchronized void connect() {
		if( !this.isEnabled() )
			throw new ServiceConnectionException( "The " + this.name + IPersistenceService.S_ERR_NO_SERVICE_FOUND );
		if( this.connected )
			return;	
		lock.lock();
		try{
			logger.info("CONNECTING Manager " + name + ": " + ( factory != null ));
			this.args = onPrepareManager();
			manager = Utils.assertNull(args)? factory.createObjectEntityManager(): 
				factory.createObjectEntityManager( args );
			if( manager == null )
				return;
			onManagerCreated(manager);
			logger.info("Manager CONNECTED " + name + ": " + ( manager != null ));
			connected = true;
			notifyListeners( Services.OPEN);

		}catch( Exception ex ){
			ex.printStackTrace();
		}
		finally{
			lock.unlock();
		}
	}

	@Override
	public boolean isConnected() {
		return this.connected;
	}

	/**
	 * Returns true if the service is open, and throws an exception otherwise
	 * @return
	 */
	@Override
	public boolean checkOpen(){
		if( !connected )
			throw new ServiceConnectionException( ServiceConnectionException.S_ERR_SERVICE_NOT_OPENED );
		return this.connected;
	}

	@Override
	public synchronized void disconnect() {
		if( !this.connected )
			return;
		this.connected = false;			
		lock.lock();
		try{
			if( this.manager != null ){
				//this.manager.clear();
				//this.manager.close();
			}
			this.manager = null;
			logger.info("DISCONNECTING Manager  " + name + ": ");
		}
		finally{
			lock.unlock();
		}
		this.notifyListeners( Services.CLOSE);
	}	

	/**
	 * Reopen the service. This is needed after an external commit, for instance when persisting a new object
	 */
	@Override
	public void reopen() {
		this.connected = false;
		this.connect();
	}	
	
	@Override
	public void addListener(
			IPersistencyServiceListener persistencyServiceListener) {
		lock.lock();
		try{
			listeners.add(persistencyServiceListener);
		}
		finally{
			lock.unlock();
		}
	}

	@Override
	public synchronized void removeListener(
			IPersistencyServiceListener persistencyServiceListener) {
		lock.lock();
		try{
			listeners.remove(persistencyServiceListener);
		}
		finally{
			lock.unlock();
		}
	}

	protected synchronized void notifyListeners( Services action) {
		lock.lock();
		try{
			for (IPersistencyServiceListener l : listeners) {
				l.notifyServiceChanged( new PersistencyServiceEvent( this, action ));
			}
		}
		finally{
			lock.unlock();
		}
	}
}