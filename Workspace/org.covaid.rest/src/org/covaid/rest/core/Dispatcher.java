package org.covaid.rest.core;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.covaid.core.data.frogger.HubData;
import org.covaid.core.def.IEnvironment;
import org.covaid.core.environment.AbstractEnvironment;
import org.covaid.core.environment.frogger.FroggerDomain;
import org.covaid.orientdb.object.AbstractPersistenceService;

import com.orientechnologies.orient.core.entity.OEntityManager;

public class Dispatcher extends AbstractPersistenceService {

	//Needs to be the same as in the persistence.xml file
	private static final String S_COVAID_SERVICE_ID = "org.covaid.rest.service"; 
	private static final String S_COVAID_SERVICE = "CovAID REST Service"; 

	private static Dispatcher dispatcher = new Dispatcher();
	
	private TreeMap<String, Environment> environments;

	private Dispatcher() {
		super( S_COVAID_SERVICE_ID, S_COVAID_SERVICE );
		environments = new TreeMap<>();
	}

	public static Dispatcher getInstance() {
		return dispatcher;
	}
	
	public IEnvironment<Integer> register( String identifier ) {
		Environment result = this.environments.get( identifier);
		if( result == null ) {
			result = new Environment( identifier );
			this.environments.put(identifier, result);
		}
		return result;
	}
	
	public boolean isRegistered( String identifier, String token ) {
		return this.environments.containsKey( identifier );
	}

	public boolean start(String identifier, int width, int density, int infected) {
		Environment result = (Environment) this.register(identifier);
		result.init( width, density, infected);
		result.start();
		return true;
	}

	public boolean stop(String identifier) {
		IEnvironment<Integer> result = this.environments.get( identifier);
		if( result == null )
			return false;
		result.stop();
		return true;
	}

	public boolean clear(String identifier) {
		IEnvironment<Integer> result = this.environments.get( identifier);
		if( result == null )
			return false;
		result.clear();
		return true;
	}

	public boolean pause(String identifier) {
		IEnvironment<Integer> result = this.environments.get( identifier);
		if( result == null )
			return false;
		return result.pause();
	}

	public boolean setInfected(String identifier, int infected) {
		Environment result = this.environments.get( identifier);
		if( result == null )
			return false;
		result.setinfected(infected);
		return true;
	}

	public Collection<HubData> getUpdate(String identifier, int step ) {
		Environment env = (Environment) this.environments.get( identifier);
		if( env == null )
			return null;
		return env.getUpdate( step );
	}

	@Override
	protected Map<String, String> onPrepareManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onManagerCreated(OEntityManager manager) {
		// TODO Auto-generated method stub
		
	}

	public void subscribe(long id, int i) {
		// TODO Auto-generated method stub	
	}

	public void dispose( String identifier ) {
		Environment env = this.environments.remove( identifier);
		env.dispose();
	}

	private class Environment extends AbstractEnvironment<Integer>{

		private FroggerDomain domain;
		
		protected Environment( String name) {
			super(name);
			this.domain = new FroggerDomain( name, this);
			super.addDomain(domain);
		}
	
		public void init(int width, int density, int infected) {
			int population = (int)((double)width*density/100);
			super.init(population);
			domain.init(width, infected, 10);
		}

		public void setinfected( int infected ) {
			domain.setInfected( infected );
		}
		
		@Override
		public Integer getTimeStep( long days ) {
			return (int) days % Integer.MAX_VALUE;
		}
		
		public Collection<HubData> getUpdate( int step ){
			return domain.getUpdate( step );
		}
	}
}
