package org.covaid.core.environment.frogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.IContagion.SupportedContagion;
import org.covaid.core.def.IEnvironment;
import org.covaid.core.def.IHub;
import org.covaid.core.def.IPerson;
import org.covaid.core.environment.AbstractDomain;
import org.covaid.core.environment.IDomain;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Person;

public class FroggerDomain extends AbstractDomain<Integer> implements IDomain<Integer>{

	public static final int DEFAULT_MAX_TIME = 60;//days
	public static final int DEFAULT_WIDTH = 100;//meters

	public static final String S_ERR_INVALID_HUB = "An invalid hub was encountered; the position must be smaller than the width: ";

	private TreeMap<Integer,DayData> days;
	private Collection<IPerson> persons;
	private int time;
	private int maxTime;

	private int width;
	private int density;
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
	public void init(int width, int density, int infected ) {
		this.width = width;
		this.density = density;
		this.infected = infected;
		int population = super.getPopulation(); 
		this.init(population);
	}

	public boolean addDay( int width, Collection<IHub> hubs ) {
		DayData data = new DayData(width);
		days.put(time, data);
		boolean result = true;
		for( IHub hub: hubs ) {
			if( hub.getLocation().getXpos() >= width )
				return false;
			result &= data.addHub(hub);
		}
		return result;
	}
	
	public boolean update( int width, Collection<IHub> hubs ) {
		boolean result = addDay(width, hubs);
		if( !result )
			throw new IllegalArgumentException( S_ERR_INVALID_HUB); 
		time++;
		days.tailMap(time);
		return result;
	}
	
	private class DayData{
		
		private int width;
		
		private Collection<IHub> hubs;

		public DayData(int width) {
			super();
			this.width = width;
			hubs = new ArrayList<>();
		}
		
		public int getWidth() {
			return width;
		}


		public boolean addHub( IHub hub ) {
			return this.hubs.add(hub);
		}
	}

	@Override
	public void movePerson(Integer timeStep) {
		//Create a row of new persons
		for( int i=0; i< super.getPopulation(); i++) {
			int x = (int) (width * Math.random());
			double safety = 100* Math.random();
			double contagion = 100*Math.random();
			String identifier = "["+ timeStep + ":" + i + "]";
			IPerson person = ( contagion < infected )? new Person( identifier, x, 0, safety, new Contagion( this.contagion, 100d )): new Person( identifier, x, 0, safety );
			persons.add(person);
			days.put(timeStep, new DayData( width));
		}
		
	}

	@Override
	public void update(Integer date) {
		// TODO Auto-generated method stub
		
	}
}