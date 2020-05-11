package org.covaid.core.environment.frogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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

	private Collection<IPerson<Integer>> persons;
	private Map<IPoint, Hub> hubs;
	private int maxTime;

	private int width;
	private int infected;
	
	private String contagion;
	
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	public FroggerDomain( String name, IEnvironment<Integer> environment ) {
		this( name, environment, SupportedContagion.COVID_19, DEFAULT_MAX_TIME );
	}
	
	public FroggerDomain( String name, IEnvironment<Integer> environment, IContagion.SupportedContagion supported, int maxTime ) {
		super( name );
		this.contagion = supported.name();
		persons = new ArrayList<>();
		hubs = new HashMap<>();
	}

	public Point getField() {
		return new Point( width, hubs.size() );
	}

	public Collection<Hub> getHubs() {
		return hubs.values();
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
	public void init(int width, int infected ) {
		this.width = width;
		this.infected = infected;
		int population = super.getPopulation(); 
		this.init(population);
	}
	
	@Override
	public synchronized void movePerson(Integer timeStep) {

		//first advance the population by moving the persons one place
		//logger.info( "TIMESTEP: " + timeStep +", " + this.hubs.size());
		//StringBuilder builder = new StringBuilder();
		//builder.append("Persons moved: ");
		for( IPerson<Integer> person: this.persons ) {
			int xpos = moveX( person );
			int ypos = person.getLocation().getYpos();
			person.setPosition(xpos, ypos+1 );
			encounter( person, timeStep );
			//builder.append( person.getIdentifier() + " => " + person.getLocation());
			//builder.append(", ");
		}
		//logger.info(builder.toString() + "\n\n");
		
		//Then create a new population
		for( int i=0; i< super.getPopulation(); i++) {
			int x = (int) (width * Math.random());
			double safety = 100* Math.random();
			String identifier = "["+ x + ":" + i + "]";
			IPerson<Integer> person = new Person( identifier, x, 0, safety );
			persons.add(person);
			encounter( person, timeStep );
		}
		//Then set the infections
		for( IPerson<Integer> person: persons) {
			double contagion = 100*Math.random();
			if( contagion < infected )
				person.setContagion( timeStep, new Contagion( this.contagion, 100));
		}
		
		//Last clear old values
		hubs.entrySet().removeIf(entry -> entry.getKey().getYpos() > DEFAULT_MAX_TIME);
		//logger.info("Hubs: " + this.hubs.size());
	}

	protected void encounter( IPerson<Integer> person, int step ) {
		Hub hub = this.hubs.get(person.getLocation());
		if( hub == null ) {
			hub = new Hub( person );
			this.hubs.put(hub.getLocation(), hub);
		}
		hub.encounter(person, step);
		
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
	public synchronized Hub[] getUpdate(){
		List<Hub> results = new ArrayList<Hub>( this.hubs.values());
		return results.toArray( new Hub[ results.size()]);
	}

	@Override
	public void update(Integer date) {
		// TODO Auto-generated method stub	
	}
}