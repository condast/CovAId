package org.covaid.rest.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import org.condast.commons.persistence.service.AbstractPersistencyService;

public class Dispatcher extends AbstractPersistencyService {

	//Needs to be the same as in the persistence.xml file
	private static final String S_COVAID_SERVICE_ID = "org.covaid.rest.service"; 
	private static final String S_COVAID_SERVICE = "CovAID REST Service"; 

	private static Dispatcher dispatcher = new Dispatcher();
	
	private Collection<Long> users;

	private Dispatcher() {
		super( S_COVAID_SERVICE_ID, S_COVAID_SERVICE );
		users = new TreeSet<>();
	}

	public static Dispatcher getInstance() {
		return dispatcher;
	}

	@Override
	protected Map<String, String> onPrepareManager() {
		Map<String, String> orientDBProp = new HashMap<String, String>(){
			private static final long serialVersionUID = 1L;
			{
				put("javax.persistence.jdbc.url", "remote:localhost/test.odb");
				put("javax.persistence.jdbc.user", "admin");
				put("javax.persistence.jdbc.password", "admin");
				put("com.orientdb.entityClasses", "com.example.domains");
			}
		};
		return orientDBProp;
	}

	
	@Override
	protected void onManagerCreated(EntityManager manager) {
		// TODO Auto-generated method stub
		
	}

	public boolean isregistered( long userId, String token ) {
		return this.users.contains(userId );
	}

	public void start(long userId, int i) {
		this.users.add(userId);
	}

	
}
