package org.covaid.core.environment.frogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.covaid.core.data.frogger.HubData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IContagion.SupportedContagion;
import org.covaid.core.def.IEnvironment;
import org.covaid.core.def.IHub;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.environment.AbstractDomain;
import org.covaid.core.environment.IDomain;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Hub;
import org.covaid.core.model.Person;
import org.covaid.core.model.Point;

public class FroggerDomain extends AbstractDomain<Integer> implements IDomain<Integer>{

	public static final int DEFAULT_MAX_TIME = 60;//days
	public static final int DEFAULT_WIDTH = 100;//meters
	public static final int DEFAULT_HISTORY = 14;//days, the visible portion of the screen

	public static final String S_ERR_INVALID_HUB = "An invalid hub was encountered; the position must be smaller than the width: ";

	private Collection<IPerson<Integer>> persons;
	private Map<IPoint, Hub> hubs;
	private int maxTime;

	private int width;
	private int infected;
	
	private String contagion;
	
	private Collection<HubData> snapshot; 
	
	public FroggerDomain( String name, IEnvironment<Integer> environment ) {
		this( name, environment, SupportedContagion.COVID_19, DEFAULT_MAX_TIME );
	}

	public FroggerDomain( String name, IEnvironment<Integer> environment, int maxTime ) {
		this( name, environment, SupportedContagion.COVID_19, maxTime );
	}
	
	public FroggerDomain( String name, IEnvironment<Integer> environment, IContagion.SupportedContagion supported, int maxTime ) {
		super( name );
		this.contagion = supported.name();
		persons = new ArrayList<>();
		this.snapshot = new ArrayList<>();
		hubs = new TreeMap<>( (o1, o2)->{
			int compare = o1.getYpos() - o2.getYpos();
			return( compare != 0)? compare: o1.getXpos() - o2.getXpos();
		});
		this.maxTime = maxTime;
	}

	public Point getField() {
		return new Point( width, hubs.size() );
	}

	public Collection<Hub> getHubs() {
		return this.hubs.values();
	}

	public int getMaxTime() {
		return maxTime;
	}

	@Override
	public void clear() {
		this.persons.clear();
		this.hubs.clear();
	}

	/**
	 * The population is the amount of people allowd in one scan 
	 * @param population
	 * @param infected
	 */
	public void init(int width, int infected, int population ) {
		this.width = width;
		this.infected = infected;
		this.init(population);
	}
	
	@Override
	public synchronized void movePerson(Integer timeStep) {

		//first advance the population by moving the persons one place
		//logger.info( "TIMESTEP: " + timeStep +", " + this.hubs.size());

		for( IPerson<Integer> person: this.persons ) {
			IPoint point = person.getLocation().clone();
			int xpos = moveX( person );
			int ypos = person.getLocation().getYpos();
			person.setPosition(xpos, ypos+1 );
			IHub<Integer> hub = encounter( person, timeStep );
			hub.addPrevious(hubs.get( point ));
		}
		
		//Then create a new population
		for( int i=0; i< super.getPopulation(); i++) {
			int x = (int) ((double)width * Math.random());
			double safety = 100* Math.random();
			String identifier = "["+ x + ":" + i + "]";
			IPerson<Integer> person = new Person( identifier, x, 0, safety );
			persons.add(person);
			encounter( person, timeStep );
		}
		//Then set the infections
		for( IPerson<Integer> person: persons) {
			double contagion = 100*Math.random();
			if( contagion < this.infected )
				person.setContagion( timeStep, new Contagion( this.contagion, 100));
		}

		
		//Last clear old values
		Iterator<Map.Entry<IPoint, Hub>> iterator = this.hubs.entrySet().iterator();
		Collection<IPoint> remove = new ArrayList<>();
		while( iterator.hasNext() ) {
			Map.Entry<IPoint, Hub> entry = iterator.next();
			if( entry.getKey().getYpos() > this.maxTime)
				remove.add(entry.getKey());
			else {
				entry.getValue().update(timeStep);
			}
		}
		for( IPoint point: remove)
			this.hubs.remove( point);
		persons.removeIf(person -> person.getLocation().getYpos() > this.maxTime);
		//logger.info("Hubs: " + this.hubs.size());
	}

	protected IHub<Integer> encounter( IPerson<Integer> person, int step ) {
		Hub hub = this.hubs.get(person.getLocation());
		if( hub == null ) {
			hub = new Hub( person );
			this.hubs.put(hub.getLocation(), hub);
		}
		hub.encounter(person, step);
		return hub;	
	}
	
	protected int moveX( IPerson<Integer> person ) {
		int xpos = person.getLocation().getXpos();
		double movement = 2;
		if(( xpos>1 ) && ( xpos <= width-1 ))
			movement +=1;
		if( xpos>1 )
			xpos -= 1; 
		xpos += movement * Math.random();
		return xpos;
	}
	
	public synchronized Collection<HubData> getUpdate(Integer step){
		return new ArrayList<HubData>( this.snapshot );
	}

	@Override
	public synchronized void update(Integer step) {
		Collection<Hub> results = new ArrayList<>();
		Iterator<Map.Entry<IPoint, Hub>> iterator = this.hubs.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<IPoint, Hub> entry = iterator.next();
			if( entry.getKey().getYpos() < step)
				results.add(entry.getValue());
		}
		//this.hubs.entrySet().stream().filter( entry -> entry.getKey().getYpos() <= step).collect(Collectors.toList());
		this.snapshot = Arrays.asList( HubData.getHubs(results, step));
	}
}