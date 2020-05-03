package org.covaid.mobile.core;

import org.condast.commons.data.plane.IField;
import org.condast.commons.persistence.service.AbstractPersistencyService;
import org.covaid.core.def.IPerson;
import org.covaid.core.field.IFieldListener;
import org.covaid.core.field.IFieldProvider;

public class Dispatcher extends AbstractPersistencyService {

	//Needs to be the same as in the persistence.xml file
	private static final String S_COVAID_SERVICE_ID = "org.covaid.rest.service"; 
	private static final String S_COVAID_SERVICE = "CovAID REST Service"; 

	private static Dispatcher dispatcher = new Dispatcher();
	
	private IPerson person;
	
	private IFieldProvider provider;
	
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

	public void setFieldProvider(IFieldProvider provider) {
		this.provider = provider;
	}

	public void removeFieldProvider(IFieldProvider provider) {
		this.provider = null;
	}

	public void addFieldListener(IFieldListener listener) {
		provider.addFieldListener(listener);
	}
	public void removeFieldListener(IFieldListener listener) {
		provider.removeFieldListener(listener);
	}

	public IField getField() {
		return this.provider.getField();
	}
	
}
