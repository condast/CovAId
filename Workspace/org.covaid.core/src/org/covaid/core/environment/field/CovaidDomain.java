package org.covaid.core.environment.field;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.IFieldEnvironment;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.environment.AbstractFieldDomain;
import org.covaid.core.hub.IHub;
import org.covaid.core.model.Point;
import org.covaid.core.model.date.DateContagion;
import org.covaid.core.model.date.DateHub;

public class CovaidDomain extends AbstractFieldDomain{

	public static final String NAME = "COVAID";

	private int radius;

	private int index;
	
	public CovaidDomain() {
		this( NAME );
	}
	
	public CovaidDomain( String name ) {
		super( name );
		this.index = 0;
	}

	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	@Override
	public void init( int population ) {
		this.index = 0;
		super.init( population );
		this.radius = DEFAULT_RADIUS;
	}
	
	@Override
	protected void onCreatePerson( IFieldDomain domain, IPerson<Date> person) {
		IFieldEnvironment env = (IFieldEnvironment) super.getEnvironment();
		
		DateContagion contagion = new DateContagion( IContagion.SupportedContagion.valueOf( env.getContagion()), 100 );
		if( index == 0 ) {
			person.setPosition((int)domain.getField().getLength()/2, (int)domain.getField().getWidth()/2);
			person.setContagion( env.getTimeStep(env.getDays()), contagion);
		}
		index++;
	}

	/**
	 * Move a person
	 * @param population
	 */
	@Override
	protected void onMovePerson( IFieldDomain domain, Date date, IPerson<Date> person) {
		//analyseHub(date, person);//Create a new hub if the person has a risk of contagion
		IFieldEnvironment env = (IFieldEnvironment) super.getEnvironment();
		DateContagion contagion = new DateContagion( IContagion.SupportedContagion.valueOf( env.getContagion()), 100 );
		Collection<IPerson<Date>> persons = domain.getPersons();
		double distance = 0;
		if( person.getContagiousness(contagion, date) > 10 ){
			for( IPerson<Date> other: persons) {
				distance = person.getLocation().getDistance(other.getLocation());
				if( contagion.getDistance() < distance)
					continue;
				if( other.getContagiousness(contagion, date) < person.getContagiousness(contagion,date))
					other.setContagion( env.getTimeStep( env.getDays()), contagion);
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
		for( IPerson<Date> other: persons ){
			distance = person.getLocation().getDistance(other.getLocation());
			if( distance > bestdistance ) {
				bestdistance = distance;
				bestLocation = new Point( x, y);
			}
			safety = other.getSafetyBubble(contagion, env.getTimeStep( env.getDays()));
			okay &= ( distance > safety ) && ( distance > risk );
			if( okay )
				bestLocation = new Point( x, y);
		}
		persons.add(person);
		person.move( bestLocation);
	}
	
	private IHub<Date> analyseHub( IFieldDomain domain, Date date, IPerson<Date> person ) {
		IPoint location = person.getLocation();
		if( person.isHealthy())
			return null;
		Map<String, IHub<Date>> hubs = domain.getHubs();
		IHub<Date> hub = hubs.get(location.getIdentifier());
		if( hub == null ) {
			hub = new DateHub( person );
			hubs.put( location.getIdentifier(), hub );
		}else {
			hub.encounter(person, date);
		}
		return hub;
	}
}