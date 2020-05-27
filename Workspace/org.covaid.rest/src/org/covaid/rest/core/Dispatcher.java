package org.covaid.rest.core;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.covaid.core.data.TimelineCollection;
import org.covaid.core.data.frogger.HubData;
import org.covaid.core.data.frogger.LocationData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IMobile;
import org.covaid.core.environment.AbstractEnvironment;
import org.covaid.core.environment.IEnvironment;
import org.covaid.core.environment.frogger.FroggerDomain;
import org.covaid.core.environment.frogger.FroggerDomain.Hubs;
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

	public ILocation<Integer> getLocation( String identifier, Hubs hub ){
		Environment result = this.environments.get( identifier);
		if( result == null )
			return null;
		return result.getLocation(hub);
	}

	public boolean setInfected(String identifier, int infected) {
		Environment result = this.environments.get( identifier);
		if( result == null )
			return false;
		result.setInfected(infected);
		return true;
	}

	public boolean setDensity(String identifier, int density) {
		Environment result = this.environments.get( identifier);
		if( result == null )
			return false;
		result.setDensity(density);
		return true;
	}

	public boolean setProtection(String identifier, boolean protection) {
		Environment result = this.environments.get( identifier);
		if( result == null )
			return false;
		result.setProtection( protection );
		return true;
	}

	public LocationData<Integer>[] getProtected(String identifier) {
		Environment result = this.environments.get( identifier);
		if( result == null )
			return null;
		return result.getProtected();
	}

	public Map<Integer, Double> getPrediction( String identifier, FroggerDomain.Hubs select, Integer range ) {
		Environment result = this.environments.get( identifier);
		if( result == null )
			return null;
		return result.getPrediction(select, range);
	}

	public TimelineCollection<Integer, Double> getAverage( String identifier, int step ) {
		Environment result = this.environments.get( identifier);
		if( result == null )
			return null;
		return result.getAverage( step);		
	}

	public Collection<HubData> getUpdate(String identifier, int step ) {
		Environment env = (Environment) this.environments.get( identifier);
		if( env == null )
			return null;
		return env.getUpdate( step );
	}

	public LocationData<Integer>[] getSurroundings( String identifier, int radius, int step ) {
		Environment env = (Environment) this.environments.get( identifier);
		if( env == null )
			return null;
		return env.getSurroundings(radius, step);
	}

	@Override
	protected Map<String, String> onPrepareManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAdvice( Hubs hub, IContagion contagion ) {
		String result = IMobile.S_INFO_NOTHING_WRONG;
		Environment env = this.environments.values().iterator().next();
		if( env == null )
			return result;
		ILocation<Integer> location = env.getLocation(hub);
		if( location == null )
			return result;
		try {
			double risk = location.getRisk(contagion, FroggerDomain.DEFAULT_HISTORY);
			if( risk > 20 ) {
				result = IMobile.S_INFO_RISK_OF_CONTAGION + contagion.getIdentifier() + "\n" + IMobile.S_INFO_EMAIL_DOCTOR;
			}
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		return result;
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
		private static final int FROGGER_SPEED = 1500;//1.5 sec
		
		private FroggerDomain domain;
		
		protected Environment( String name) {
			super(name, FROGGER_SPEED);
			this.domain = new FroggerDomain( name, this);
			super.addDomain(domain);
		}
	
		public void init(int width, int density, int infected) {
			int population = (int)((double)width*density/100);
			super.init(population);
			domain.init(width, infected, 10);
		}
		
		@Override
		public void clear() {
			domain.clear();
			super.clear();
		}

		public ILocation<Integer> getLocation( Hubs hub ){
			return domain.getLocation(hub);
		}

		public void setInfected( int infected ) {
			domain.setInfected( infected );
		}

		public void setDensity(int density) {
			int population = domain.setDensity(density);
			super.setPopulation(population);
		}

		@Override
		public Integer getTimeStep( long days ) {
			return (int) days % Integer.MAX_VALUE;
		}

		public void setProtection(boolean protection) {
			domain.setProtection(protection);
		}

		public LocationData<Integer>[] getProtected() {
			return domain.getProtected();
		}

		public Map<Integer, Double> getPrediction( FroggerDomain.Hubs select, Integer range ) {
			return this.domain.getPrediction(select, range);
		}

		public TimelineCollection<Integer, Double> getAverage( int step ) {
			return domain.getAverage(step);
		}

		public Collection<HubData> getUpdate( int step ){
			return domain.getUpdate( step );
		}

		public LocationData<Integer>[] getSurroundings( int radius, int step ){
			return domain.getSurroundings(radius, step);
		}
	}
}
