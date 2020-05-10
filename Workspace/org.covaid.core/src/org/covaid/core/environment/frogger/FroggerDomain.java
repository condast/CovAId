package org.covaid.core.environment.frogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

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

	public static final String S_ERR_INVALID_HUB = "An invalid hub was encountered; the position must be smaller than the width: ";

	private TreeMap<Integer,DayData> days;
	private Collection<IPerson<Integer>> persons;
	private int time;
	private int maxTime;

	private int width;
	private int infected;
	
	private String contagion;
	
	public FroggerDomain( String name, IEnvironment<Integer> environment ) {
		this( name, environment, SupportedContagion.COVID_19, DEFAULT_MAX_TIME );
	}
	
	public FroggerDomain( String name, IEnvironment<Integer> environment, IContagion.SupportedContagion supported, int maxTime ) {
		super( name );
		this.contagion = supported.name();
		days = new TreeMap<>();
		time = 0;
		this.persons = new ArrayList<>();
	}

	public Map<Integer, DayData> getDays() {
		return days;
	}

	public int getTime() {
		return time;
	}

	public int getMaxTime() {
		return maxTime;
	}

	@Override
	public void clear() {
		this.time = 0;
		this.days.clear();
	}

	/**
	 * The population is the amount of people allowd in one scan 
	 * @param population
	 * @param infected
	 */
	public void init(int width, int infected ) {
		this.width = width;
		this.infected = infected;
		int population = super.getPopulation(); 
		this.init(population);
	}
	
	public boolean update( int width, Collection<IHub<Integer>> hubs ) {
		boolean result = false;//addDay(width, hubs);
		if( !result )
			throw new IllegalArgumentException( S_ERR_INVALID_HUB); 
		time++;
		days.tailMap(time);
		return result;
	}
	
	@Override
	public void movePerson(Integer timeStep) {
		
		//first advance the population by creating a new set of hubs at timestamp and 
		//moving the persons one place
		DayData data = new DayData();
		this.days.put(timeStep, data);
		for( int i=timeStep-1; i>0; i-- ) {
			DayData current = days.get(timeStep-1);
			for( IPoint point: current.hubs.keySet()) {
				IHub<Integer> source = current.hubs.get(point);
				IHub<Integer> target = data.hubs.get(point);
				for( IPerson<Integer> person: source.getPersons().values())
					target.encounter(person, i);
				data = current;
			}
		}

		//Create a row of new persons
		data = days.get(0);
		for( int i=0; i< super.getPopulation(); i++) {
			int x = (int) (width * Math.random());
			double safety = 100* Math.random();
			String identifier = "["+ timeStep + ":" + i + "]";
			IPerson<Integer> person = new Person( identifier, x, 0, safety );
			persons.add(person);
			data.addHub(0, person);
		}
		//Then set the infections
		for( IPerson<Integer> person: this.persons) {
			double contagion = 100*Math.random();
			if( contagion < infected )
				person.setContagion( timeStep, new Contagion( this.contagion, 100));
		}
	}

	public Map<Integer, Map<Point, Hub>> getUpdate(){
		Map<Integer, Map<Point,Hub>> results = new HashMap<>();
		Iterator<Map.Entry<Integer, DayData>> iterator = this.days.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Integer, DayData> entry = iterator.next();
			results.put(entry.getKey(), entry.getValue().hubs);
		}
		return results;
	}

	@Override
	public void update(Integer date) {
		// TODO Auto-generated method stub	
	}

	private class DayData{
		
		private Map<Point, Hub> hubs;

		public DayData() {
			super();
			hubs = new HashMap<>();
		}
		
		public IHub<Integer> addHub( int timeStep, IPerson<Integer> person ) {
			IHub<Integer> hub = hubs.get( person.getLocation());
			if( hub == null ) {
				hub = new Hub( person);
				this.hubs.put( (Point) person.getLocation(), (Hub) hub );
			}else
				hub.encounter(person, timeStep);
			return hub;
		}
		
	}
}