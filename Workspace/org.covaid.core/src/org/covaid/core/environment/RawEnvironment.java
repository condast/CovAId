package org.covaid.core.environment;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.condast.commons.data.latlng.LatLng;
import org.covaid.core.def.AbstractEnvironment;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Hub;
import org.covaid.core.model.Person;
import org.covaid.core.model.Point;

public class RawEnvironment extends AbstractEnvironment{

	public static final String NAME = "RAW";
	public static final int DEFAULT_RADIUS = 10;//metres, the maximum movement that a person can make during one step in activity

	private int radius;
		
	public RawEnvironment() {
		super( NAME );
	}
	
	public RawEnvironment( int length, int width, int population ) {
		this( new LatLng( NAME, LATITUDE, LONGITUDE), length, width, population, DEFAULT_SPEED);
	}

	public RawEnvironment( LatLng location, int length, int width, int population, int speed ) {
		super( location, length, width, population, speed );
	}

	public void init( int population ) {
		init( DEFAULT_RADIUS, DEFAULT_ACTIVITY, population );
	}
	
	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	public void init( int radius, int activity, int population ) {
		super.init(activity, population);
		this.radius = radius;
	}
	
	@Override
	protected void onCreatePerson(int index, IPerson person) {
		Contagion contagion = IContagion.SupportedContagion.valueOf(getContagion()).getContagion();
		if( index == 0 )
			person.setContagion(getDate(), contagion);
	}
	
	/**
	 * Move a person
	 * @param population
	 */
	protected void onMovePerson( Date date, Person person) {
		//analyseHub(date, person);//Create a new hub if the person has a risk of contagion
		Contagion contagion = IContagion.SupportedContagion.getContagion(getContagion());
		Collection<Person> persons = super.getPersons();
		if( person.getContagiousness(contagion) > 10 ){
			for( IPerson other: persons) {
				double distance = person.getLocation().getDistance(other.getLocation());
				if( contagion.getDistance() < distance)
					continue;
				if( other.getContagiousness(contagion) < person.getContagiousness(contagion))
					other.setContagion(getDate(), contagion);
			}
		}
		persons.remove(person);
		int x = person.getLocation().getXpos();
		int y = person.getLocation().getYpos();
		do {
			x = person.getLocation().getXpos() + (int)( radius * (Math.random() - 0.5f));
			y = person.getLocation().getYpos() + (int)( radius * (Math.random() - 0.5f));
			person.setPosition(x, y);
		}
		while(persons.contains(person));
		persons.add(person);
		person.move(new Point( x, y));
	}

	
	private Hub analyseHub( Date date, IPerson person ) {
		IPoint location = person.getLocation();
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