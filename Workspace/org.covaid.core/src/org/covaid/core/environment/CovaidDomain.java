package org.covaid.core.environment;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.IEnvironment;
import org.covaid.core.def.IHub;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.Contagion;
import org.covaid.core.model.Hub;
import org.covaid.core.model.Point;

public class CovaidDomain extends AbstractDomain{

	public static final String NAME = "COVAID";

	private int radius;

	private int index;
	private Logger logger = Logger.getLogger(this.getClass().getName());
	
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
	protected void onCreatePerson( AbstractDomain domain, IPerson person) {
		IEnvironment env = super.getEnvironment();
		Contagion contagion = IContagion.SupportedContagion.valueOf( env.getContagion()).getContagion();
		if( index == 0 ) {
			person.setPosition((int)domain.getField().getLength()/2, (int)domain.getField().getWidth()/2);
			person.setContagion( env.getDate(), contagion);
		}
		index++;
	}

	/**
	 * Move a person
	 * @param population
	 */
	@Override
	protected void onMovePerson( AbstractDomain domain, Date date, IPerson person) {
		//analyseHub(date, person);//Create a new hub if the person has a risk of contagion
		IEnvironment env = super.getEnvironment();
		Contagion contagion = IContagion.SupportedContagion.getContagion( env.getContagion());
		Collection<IPerson> persons = domain.getPersons();
		double distance = 0;
		if( person.getContagiousness(contagion) > 10 ){
			for( IPerson other: persons) {
				distance = person.getLocation().getDistance(other.getLocation());
				if( contagion.getDistance() < distance)
					continue;
				if( other.getContagiousness(contagion) < person.getContagiousness(contagion))
					other.setContagion( env.getDate(), contagion);
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
		for( IPerson other: persons ){
			distance = person.getLocation().getDistance(other.getLocation());
			if( distance > bestdistance ) {
				bestdistance = distance;
				bestLocation = new Point( x, y);
			}
			safety = other.getSafetyBubble(contagion, env.getDate());
			okay &= ( distance > safety ) && ( distance > risk );
			if( okay )
				bestLocation = new Point( x, y);
		}
		persons.add(person);
		person.move( bestLocation);
	}
	
	private IHub analyseHub( AbstractDomain domain, Date date, IPerson person ) {
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