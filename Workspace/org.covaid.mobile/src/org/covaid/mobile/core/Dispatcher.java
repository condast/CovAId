package org.covaid.mobile.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.condast.commons.data.plane.IField;
import org.condast.commons.project.ProjectFolderUtils;
import org.covaid.core.def.IMobile;
import org.covaid.core.field.IFieldListener;
import org.covaid.core.field.IFieldProvider;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Point;
import org.covaid.core.model.date.DateHistory;
import org.covaid.core.model.date.DateLocation;
import org.covaid.core.model.date.DateMobile;
import org.covaid.core.model.date.DatePerson;
import org.covaid.mobile.Activator;
import org.covaid.orientdb.object.AbstractPersistenceService;
import org.covaid.orientdb.object.IOrientEntityManagerFactory;

import com.orientechnologies.orient.core.entity.OEntityManager;

public class Dispatcher extends AbstractPersistenceService{

	//Needs to be the same as in the persistence.xml file
	private static final String S_COVAID_SERVICE_ID = "org.covaid.mobile.service"; 
	private static final String S_COVAID_SERVICE = "CovAID MOBILE Service"; 

	protected static final String S_LOCAL = "plocal:";
	protected static final String S_FILE = "file:";	

	private static Dispatcher dispatcher = new Dispatcher();
	
	private Map<String, IMobile<Date>> mobiles;
	
	private IFieldProvider provider;
	
	private Properties config;
	
	private Dispatcher() {
		super( S_COVAID_SERVICE_ID, S_COVAID_SERVICE );
		mobiles = new HashMap<>();
	}

	public static Dispatcher getInstance() {
		return dispatcher;
	}
	
	public Properties getConfig() {
		return config;
	}

	public void addMobile( IMobile<Date> mobile ) {
		this.mobiles.put(mobile.getIdentifier(), mobile);
	}

	public void removeMobile( String identifier ) {
		this.mobiles.remove( identifier );
	}

	public IMobile<Date> getMobile( String identifier ){
		return this.mobiles.get(identifier);
	}
	
	@Override
	protected Map<String, String> onPrepareManager() {
		File dir = new File( ProjectFolderUtils.getDefaultUserDir( Activator.BUNDLE_ID));
		dir.setReadable(true);
		dir.setWritable(true);
		try {
			if( !dir.exists())
				Files.createDirectory(dir.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		String source = dir.toURI().toString();
		source += "/covaid.odt";  
		source = source.replace( S_FILE, S_LOCAL);		

		Map<String, String> orientDBProp = new HashMap<String, String>();
		orientDBProp.put("javax.persistence.jdbc.url", source);
		orientDBProp.put("javax.persistence.jdbc.user", "admin");
		orientDBProp.put("javax.persistence.jdbc.password", "admin");
		return orientDBProp;
	}

	@Override
	protected void onManagerCreated( OEntityManager manager) {
		OEntityManager om = (OEntityManager) manager;
		try {
			om.registerEntityClasses( Point.class, false);
			om.registerEntityClasses( Contagion.class, false);
			om.registerEntityClasses( DateLocation.class, false);
			om.registerEntityClasses( DateHistory.class, false);
			om.registerEntityClasses( DatePerson.class, false);
			om.registerEntityClasses( DateMobile.class, false);
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
	}

	@Override
	public synchronized void setEMF( IOrientEntityManagerFactory factory) {
		super.setEMF(factory);
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

	public void addFieldListener(IMobile<Date> mobile) {
		//provider.addFieldListener(mobile);
	}
	public void removeFieldListener(IFieldListener listener) {
		provider.removeFieldListener(listener);
	}

	public IField getField() {
		return this.provider.getField();
	}
}
