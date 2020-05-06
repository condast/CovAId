package org.covaid.orientdb.object;

import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import java.util.Map;

public interface IOrientEntityManagerFactory{

	public enum Type{
		DOCUMENT,
		GRAPH,
		OBJECT;
	}

	public OEntityManager createObjectEntityManager();

	public OEntityManager createObjectEntityManager( Map<String, String> properties );

	public OObjectDatabaseTx getDatabase();
}
