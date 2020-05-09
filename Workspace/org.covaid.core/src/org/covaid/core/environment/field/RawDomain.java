package org.covaid.core.environment.field;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.IFieldEnvironment;
import org.covaid.core.def.IHub;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.environment.AbstractFieldDomain;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Hub;
import org.covaid.core.model.Point;

public class RawDomain extends AbstractFieldDomain{

	public static final String NAME = "RAW";
	public static final int DEFAULT_RADIUS = 10;//metres, the maximum movement that a person can make during one step in activity

	private int radius;
	private int index;
		
	public RawDomain() {
		this( NAME );
	}

	public RawDomain( String name ) {
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
		super.init( population);
		this.radius = DEFAULT_RADIUS;
	}
	
	@Override
	protected void onCreatePerson( IFieldDomain domain, IPerson person) {
		IFieldEnvironment env = (IFieldEnvironment) super.getEnvironment();
		Contagion contagion = IContagion.SupportedContagion.valueOf(env.getContagion()).getContagion();
		if( index == 0 ) {
			person.setPosition((int)domain.getField().getLength()/2, (int)domain.getField().getWidth()/2);
			person.setContagion( env.getTimeStep(), contagion);
		}
		index++;
	}
	
	/**
	 * Move a person
	 * @param population
	 */
	@Override
	protected void onMovePerson( IFieldDomain domain, Date date, IPerson person) {
		//analyseHub(date, person);//Create a new hub if the person has a risk of contagion
		IFieldEnvironment env = (IFieldEnvironment) super.getEnvironment();
		Contagion contagion = IContagion.SupportedContagion.getContagion( env.getContagion());
		Collection<IPerson> persons = domain.getPersons();
		if( person.getContagiousness(contagion) > 10 ){
			for( IPerson other: persons) {
				double distance = person.getLocation().getDistance(other.getLocation());
				if( contagion.getDistance() < distance)
					continue;
				if( other.getContagiousness(contagion) < person.getContagiousness(contagion))
					other.setContagion( env.getTimeStep(), contagion);
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

	
	private IHub analyseHub( IFieldDomain domain, Date date, IPerson person ) {
		IPoint location = person.getLocation();
		if( person.isHealthy())
			return null;
		Map<String, IHub> hubs = domain.getHubs();
		IHub hub = hubs.get(location.getIdentifier());
		if( hub == null ) {
			hub = new Hub( person );
			hubs.put( location.getIdentifier(), hub );
		}else {
			hub.encounter(person, date);
		}
		return hub;
	}
}