package org.covaid.core.environment.frogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.covaid.core.data.frogger.HubData;
import org.covaid.core.data.frogger.LocationData;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IContagion.SupportedContagion;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.environment.AbstractDomain;
import org.covaid.core.environment.IDomain;
import org.covaid.core.environment.IEnvironment;
import org.covaid.core.hub.IHub;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Hub;
import org.covaid.core.model.Location;
import org.covaid.core.model.Person;
import org.covaid.core.model.Point;

public class FroggerDomain extends AbstractDomain<Integer> implements IDomain<Integer>{

	public static final int DEFAULT_MAX_TIME = 30;//days
	public static final int DEFAULT_WIDTH = 100;//meters
	public static final int DEFAULT_HISTORY = 14;//days, the visible portion of the screen

	public static final String S_ERR_INVALID_HUB = "An invalid hub was encountered; the position must be smaller than the width: ";

	private Collection<IPerson<Integer>> persons;
	private Map<IPoint, Hub> hubs;
	//private int window; // the window is the number of days that are in the future 
	private int maxTime;//the maximum number of days that is registered

	private int width;
	private int infected;
	
	private IContagion<Integer> contagion;
	
	private boolean protection;
	private ILocation<Integer> centre;
	private ILocation<Integer> protect;

	private Collection<HubData> snapshot; 
	
	public FroggerDomain( String name, IEnvironment<Integer> environment ) {
		this( name, environment, SupportedContagion.COVID_19, DEFAULT_MAX_TIME );
	}

	public FroggerDomain( String name, IEnvironment<Integer> environment, int maxTime ) {
		this( name, environment, SupportedContagion.COVID_19, maxTime );
	}
	
	public FroggerDomain( String name, IEnvironment<Integer> environment, IContagion.SupportedContagion supported, int maxTime ) {
		super( name );
		this.contagion = new Contagion( supported);
		persons = new ArrayList<>();
		this.snapshot = new ArrayList<>();
		hubs = new TreeMap<>( (o1, o2)->{
			int compare = o1.getYpos() - o2.getYpos();
			return( compare != 0)? compare: o1.getXpos() - o2.getXpos();
		});
		this.maxTime = maxTime;
	}

	/**
	 * The population is the amount of people allowd in one scan 
	 * @param population
	 * @param infected
	 */
	public void init(int width, int infected, int population ) {
		this.width = width;
		this.infected = infected;
		this.centre = new Location( new Point( width/2, DEFAULT_HISTORY));
		this.protect = new Location( centre.toPoint() );
		this.init(population);
	}

	public IContagion<Integer> getContagion() {
		return contagion;
	}

	public Point getField() {
		return new Point( width, hubs.size() );
	}

	public Collection<Hub> getHubs() {
		return this.hubs.values();
	}

	public Collection<IHub<Integer>> getHubs( int ypos ) {
		Collection<IHub<Integer>> result = hubs.entrySet().stream().filter(e -> e.getKey().getYpos() == ypos)
				.map( Map.Entry::getValue).
				collect(Collectors.toList());
		return result;
	}

	@Override
	public int getPopulation() {
		return this.persons.size();
	}

	public int getMaxTime() {
		return maxTime;
	}

	@Override
	public void clear() {
		this.persons.clear();
		this.hubs.clear();
	}

	public void setInfected(int infected) {
		this.infected = infected;
	}

	public void setProtection(boolean protection) {
		this.protection = protection;
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
			int moment = (int) (timeStep*Math.random());
			if( contagion < this.infected )
				person.setContagion( moment, this.contagion );
		}
		
		//Last clear old values and update the hubs
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
		handleProtection(timeStep);
		
		for( IPoint point: remove)
			this.hubs.remove( point);
		persons.removeIf(person -> person.getLocation().getYpos() > this.maxTime);
		//logger.info("Hubs: " + this.hubs.size());
	}

	protected IHub<Integer> encounter( IPerson<Integer> person, int step ) {
		Hub hub = this.hubs.get(person.getLocation());
		if( hub == null ) {
			hub = new Hub( person, step, DEFAULT_MAX_TIME );
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

	protected IPoint handleProtection( int timeStep ) {
		IPoint point = protect.toPoint();
		IPoint lowest = point;
		if(!this.protection)
			return lowest;

		IHub<Integer> centreHub = this.hubs.get(point);
		double risk = ( centreHub == null )?0: centreHub.getContagion(this.contagion, timeStep);
		if( risk == 0 )
			return lowest;
		double test = risk;
		ILocation<Integer> ld;
		for( int i=0;i<3;i++ ) {
			for( int j=0; j<3;j++) {
				int x = protect.getXpos()+i-1;
				x = (x<0)?0:(x>DEFAULT_WIDTH)?DEFAULT_WIDTH:x;
				int y = protect.getYpos()+j-1;
				y = (y<0)?0:(y>DEFAULT_HISTORY)?DEFAULT_HISTORY:y;
				point = new Point( x, y);
				IHub<Integer> testHub = this.hubs.get(point);
				if( testHub == null )
					continue;
				ld = testHub.getLocation();
				test = ld.getContagion( this.contagion, timeStep);
				if( test < risk) {
					lowest = point;
					risk = test;
				}
			}
		}

		//Update the risk of contagion in the centre
		double avg = ( this.centre.getContagion(contagion, timeStep) + centreHub.getLocation().getContagion(contagion, timeStep))/2;
		centre.setRisk(contagion, timeStep, avg);

		this.protect.move(lowest);
		IHub<Integer> loc = this.hubs.get(lowest);
		if( loc != null ) {
			avg = ( this.protect.getContagion(contagion, timeStep) + loc.getContagion(contagion, timeStep))/2;
			this.protect.setRisk(contagion, timeStep, avg);
		}
		return lowest;
	}

	@SuppressWarnings("unchecked")
	public LocationData<Integer>[] getProtected() {
		LocationData<Integer>[] results = new LocationData[2];
		results[0] = centre.toLocationData();
		results[1] = protect.toLocationData();
		return results;
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

	public synchronized LocationData<Integer>[] getSurroundings( int radius, Integer step) {
		Collection<Hub> results = new ArrayList<>();
		Iterator<Map.Entry<IPoint, Hub>> iterator = this.hubs.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<IPoint, Hub> entry = iterator.next();
			boolean valid = ( Math.abs(entry.getKey().getXpos() - protect.getXpos())<=radius ) && 
					( Math.abs(entry.getKey().getYpos() - protect.getYpos())<radius );
			if( valid )
				results.add(entry.getValue());
		}
		return HubData.getSurroundings(results, protect, radius, step );
	}
}