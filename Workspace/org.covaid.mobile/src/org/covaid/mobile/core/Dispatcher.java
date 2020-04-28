package org.covaid.mobile.core;

import org.condast.commons.persistence.service.AbstractPersistencyService;
import org.covaid.core.def.IPerson;

public class Dispatcher extends AbstractPersistencyService {

	//Needs to be the same as in the persistence.xml file
	private static final String S_COVAID_SERVICE_ID = "org.covaid.rest.service"; 
	private static final String S_COVAID_SERVICE = "CovAID REST Service"; 

	private static Dispatcher dispatcher = new Dispatcher();
	
	private IPerson person;

	private Dispatcher() {
		super( S_COVAID_SERVICE_ID, S_COVAID_SERVICE );
	}

	public static Dispatcher getInstance() {
		return dispatcher;
	}

	public boolean isregistered(long id, String token) {
		// TODO Auto-generated method stub
		return false;
	}

	
}
