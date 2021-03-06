package org.covaid.orientdb.object;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.List;

import javax.persistence.TypedQuery;

import org.condast.commons.IUpdateable;
import org.covaid.orientdb.def.IOrientPersistenceService;

import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public abstract class AbstractEntityService<O extends Object> {

	public static final String S_FIND_ALL_MULTIPLE_EMAILS_QUERY = "SELECT o FROM ";
	
	private IOrientPersistenceService service;
	private OEntityManager manager;
	private boolean connected;
	
	private Class<?> clss;
	
	public AbstractEntityService( Class<?> clss, IOrientPersistenceService service) {
		super();
		this.connected = false;
		service.connect();
		this.connected = true;
		this.service = service;
		this.clss = clss;
		manager = service.getManager();
	}
	
	public boolean isConnected() {
		return connected;
	}

	protected IOrientPersistenceService getService() {
		return service;
	}

	protected OEntityManager getManager() {
		return manager;
	}

	public boolean open() {
		OObjectDatabaseTx db = this.service.open();
		db.activateOnCurrentThread();
		return true;
	}

	public void close() {
		this.service.close();
	}

	/**
	 * Does not need open or close
	 * @param id
	 * @return
	 */
	public O find( long id ) {
		return null;//manager.find( clss, id);
	}

	/**
	 * Get the Select all query. add additional parameters if required
	 * Creates SELECT o FROM <classname> o <querystr>
	 * @param id
	 * @return
	 */
	protected String createFindString( String querystr ) {
		return S_FIND_ALL_MULTIPLE_EMAILS_QUERY + clss.getSimpleName() + " o " + querystr;
	}

	/**
	 * Get the Select all query. add additional parameters if required
	 * Creates SELECT o FROM <classname> o <querystr>
	 * @param id
	 * @return
	 */
	public List<O> findAll( String querystr ) {
		return query( createFindString(querystr));
	}

	/**
	 * Get the Select all query. add additional parameters if required
	 * Creates SELECT o FROM <classname> O 
	 * @param id
	 * @return
	 */
	public List<O> findAll() {
		return findAll("");
	}

	/**
	 * Does not need open or close
	 * @param id
	 * @return
	 */
	public List<O> query( String querystr ) {
		List<O> results =  this.service.getDatabase().query( new OSQLSynchQuery<O>( querystr ));
		for( O result: results)
			this.service.getDatabase().detach(result);
		return results;
	}

	/**
	 * Does not need open or close
	 * @param id
	 * @return
	 */
	protected TypedQuery<O> getTypedQuery( String querystr ) {
		return null;//manager.createQuery( querystr, clss );
	}

	/**
	 * Create a new object
	 * @param obj
	 */
	@SuppressWarnings("unchecked")
	protected O create( Class<?> clss ) {
		O result = (O) service.getDatabase().newInstance(clss.getSimpleName());
		if( result instanceof IUpdateable ) {
			IUpdateable updateable = (IUpdateable) result;
			updateable.setCreateDate( Calendar.getInstance().getTime());
			updateable.setUpdateDate( Calendar.getInstance().getTime());
		}
		return result;
	}

	/**
	 * Create a new object
	 * @param obj
	 */
	public void update( O obj ) {
		if( obj instanceof IUpdateable) {
			IUpdateable updateable = (IUpdateable) obj;
			updateable.setUpdateDate( Calendar.getInstance().getTime());
		}
		service.getDatabase().attach(obj);
		service.getDatabase().save(obj);
	}

	/**
	 * Remove the object with the given id. 
	 * @param id
	 * @return true if the object existed prior to removal
	 */
	public boolean remove( O obj ) {
		return ( service.getDatabase().delete(obj) != null );	
	}
	
	/**
	 * Remove all the given objects
	 * @param objects
	 */
	public void removeAll( O[] objects) {
		//for( O obj: objects ) {
		//	manager.remove( obj );
		//}
	}

	@SuppressWarnings("unchecked")
	protected O createObject( Class<O> clss, String className){
		Class<O> builderClass;
		O obj = null;
		try {
			builderClass = (Class<O>) clss.getClassLoader().loadClass( className );
			Constructor<O> constructor = builderClass.getConstructor();
			obj = constructor.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
}