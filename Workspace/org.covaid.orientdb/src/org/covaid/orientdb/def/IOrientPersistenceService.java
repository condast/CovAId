package org.covaid.orientdb.def;

import java.util.Map;

import org.condast.commons.service.IPersistencyServiceListener;
import org.covaid.orientdb.object.IOrientEntityManagerFactory;

import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public interface IOrientPersistenceService {

	public enum Attributes{
		URL,
		USER,
		PASSWORD;
		
		public String toJdbcProperty() {
			String result = "javax.persistence.jdbc." + name().toLowerCase();
			return result;
		}
	}
	
	String getId();

	String getName();

	/**
	 * returns true if the service is connected
	 * @return
	 */
	boolean isEnabled();

	OEntityManager getManager();

	void setEMF(IOrientEntityManagerFactory factory);

	void connect();

	boolean isConnected();

	/**
	 * Returns true if the service is open, and throws an exception otherwise
	 * @return
	 */
	boolean checkOpen();

	void disconnect();

	/**
	 * Reopen the service. This is needed after an external commit, for instance when persisting a new object
	 */
	void reopen();

	void addListener(IPersistencyServiceListener persistencyServiceListener);

	void removeListener(IPersistencyServiceListener persistencyServiceListener);

	OObjectDatabaseTx getDatabase();

	Map<String, String> getArgs();

}