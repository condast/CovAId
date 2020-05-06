package org.covaid.orientdb.service;

import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.condast.commons.project.ProjectFolderUtils;
import org.covaid.orientdb.Activator;
import org.covaid.orientdb.core.OrientClassHandler;
import org.covaid.orientdb.object.IOrientEntityManagerFactory;
import org.osgi.service.component.annotations.Component;

@Component(name="org.covaid.orientdb.entity.manager")
public class EntityManagerService implements IOrientEntityManagerFactory{

	protected static final String COVAID = "covaid";
	protected static final String S_LOCAL = "plocal:";
	protected static final String S_FILE = "file:";	
	protected static final String S_URL ="javax.persistence.jdbc.url";
	protected static final String S_USERNAME ="javax.persistence.jdbc.user";
	protected static final String S_PASSWORD ="javax.persistence.jdbc.password";
	
	private String source;
	private OObjectDatabaseTx dbx;
	
	public EntityManagerService() {
		source = createSource(COVAID);
	}

	@Override
	public OEntityManager createObjectEntityManager() {
		this.dbx = new OObjectDatabaseTx ( createSource(source));
		if(!dbx.exists())
			dbx.create();
		return dbx.getEntityManager();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public OEntityManager createObjectEntityManager(Map map) {
		this.source = (String) map.get(S_URL);
		String user = (String) map.get(S_USERNAME);
		String password = (String) map.get(S_PASSWORD);
		
		this.dbx = new OObjectDatabaseTx ( source );
		if(!dbx.exists())
			dbx.create();
		OEntityManager result = dbx.getEntityManager();
		result.setClassHandler(new OrientClassHandler());
		return result;
	}	
	
	@Override
	public OObjectDatabaseTx getDatabase() {
		return dbx;
	}

	protected String createSource( String name ) {
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
		source += "/" + name + ".odt";  
		source = source.replace( S_FILE, S_LOCAL);		
		return source;
	}
}
