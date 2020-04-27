package org.covaid.core.environment;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import org.condast.commons.data.latlng.LatLng;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Contagion.SupportedContagion;
import org.covaid.core.model.Hub;
import org.covaid.core.model.Person;
import org.covaid.core.model.Point;

public class CovaidEnvironment extends AbstractEnvironment{

	public static final String NAME = "COVAID";
	public static final int DEFAULT_RADIUS = 10;//metres, the maximum movement that a person can make during one step in activity

	private int radius;

	private Logger logger = Logger.getLogger(this.getClass().getName());
	
	public CovaidEnvironment() {
		this( DEFAULT_LENGTH, DEFAULT_WIDTH, DEFAULT_POPULATION );
	}
	
	public CovaidEnvironment( int length, int width, int population ) {
		super( new LatLng( NAME, LATITUDE, LONGITUDE), length, width, population, DEFAULT_SPEED);
	}

	public void init( int population ) {
		init( DEFAULT_RADIUS, DEFAULT_ACTIVITY, population );
	}
	
	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	public void init( int radius, int activity, int population ) {
		super.init( activity, population );
		this.radius = radius;
	}
	
	@Override
	protected void onCreatePerson(int index, Person person) {
		Contagion contagion = Contagion.SupportedContagion.valueOf(getContagion()).getContagion();
		if( index == 0 )
			person.setContagion(getDate(), contagion);
	}

	/**
	 * Move a person
	 * @param population
	 */
	protected void onMovePerson( Date date, Person person) {
		//analyseHub(date, person);//Create a new hub if the person has a risk of contagion
		Contagion contagion = SupportedContagion.getContagion(getContagion());
		Collection<Person> persons = super.getPersons();
		double distance = 0;
		if( person.getContagiousness(contagion) > 10 ){
			for( Person other: persons) {
				distance = person.getLocation().getDistance(other.getLocation());
				if( contagion.getDistance() < distance)
					continue;
				if( other.getContagiousness(contagion) < person.getContagiousness(contagion))
					other.setContagion(getDate(), contagion);
			}
		}
		persons.remove(person);
		int x = person.getLocation().getXpos();
		int y = person.getLocation().getYpos();
		double risk = person.getRiskBubble(contagion);
		double safety = 0;
		boolean okay = true;
		double bestdistance=  0;
		Point bestLocation = null;
		
		x = person.getLocation().getXpos() + (int)( radius * (Math.random() - 0.5f));
		y = person.getLocation().getYpos() + (int)( radius * (Math.random() - 0.5f));
		for( Person other: persons ){
			distance = person.getLocation().getDistance(other.getLocation());
			if( distance > bestdistance ) {
				bestdistance = distance;
				bestLocation = new Point( x, y);
			}
			safety = other.getSafetyBubble(contagion, getDate());
			okay &= ( distance > safety ) && ( distance > risk );
			if( okay )
				bestLocation = new Point( x, y);
		}
		persons.add(person);
		person.move( bestLocation);
	}
	
	private Hub analyseHub( Date date, Person person ) {
		Point location = person.getLocation();
		if( person.isHealthy())
			return null;
		Map<String, Hub> hubs = super.getHubs();
		Hub hub = hubs.get(location.getIdentifier());
		if( hub == null ) {
			hub = new Hub( person );
			hubs.put( location.getIdentifier(), hub );
		}else {
			hub.encounter(date, person);
		}
		return hub;
	}
}