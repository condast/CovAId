package org.covaid.rest.core;

import java.util.Collection;
import java.util.TreeSet;

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

	public boolean isregistered( long userId, String token ) {
		return this.users.contains(userId );
	}

	public void start(long userId, int i) {
		this.users.add(userId);
	}

	
}
