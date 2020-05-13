package org.covaid.core.model.date;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.condast.commons.Utils;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHub;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.model.AbstractHub;

public class DateHub extends AbstractHub<Date> implements IHub<Date> {

	public DateHub(String identifier, int xpos, int ypos) {
		this(new DateLocation( identifier, xpos, ypos ));
	}

	public DateHub( ILocation<Date> location) {
		super(location);
	}

	/**
	 * convenience method to 
	 * @param person
	 */
	public DateHub( IPerson<Date> person ) {
		this( person.getLocation().getIdentifier(), person.getLocation().getXpos(), person.getLocation().getYpos());
	}
	
	/**
	 * Respond to an encounter with a person. This happens when a person enters the location of this hub
	 * The snapshots of the person and the location are compared, and the person is alerted
	 * if the risk of infection has increased. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	@Override
	public boolean encounter( IPerson<Date> person, Date date ) {
		if( person.isHealthy() || !person.getLocation().getIdentifier().equals( super.getIdentifier()))
			return false;
		ILocation<Date> check = person.createSnapshot();

		ILocation<Date> location = super.getLocation();
		
		//Determine the worst case situation for the encounter
		ILocation<Date> worst = location.createWorst( check);
		
		//Check if the person is worse off, and if so generate an alert		
		IContagion<Date>[] worse = check.getWorse( worst );
		if( Utils.assertNull(worse)) {
			person.alert( date, worst);
		}
		
		//If the hub has deteriorated, then add the person 
		worse = location.getWorse( worst );
		if( Utils.assertNull(worse))
			location = worst;
			
		super.getPersons().put( date, person );
		return true;
	}
	
	/**
	 * Respond to an alert of a person. This happens when the person has become infected, and is alerting previous locations 
	 * of the infection. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	@Override
	public boolean alert( IPerson<Date> person, Date date ) {
		if( person.isHealthy() || !person.getLocation().getIdentifier().equals( super.getIdentifier()))
			return false;
		
		super.setLocation( createSnapShot());
		Iterator<Map.Entry<Date, IPerson<Date>>> iterator = super.getPersons().entrySet().stream()
				.filter(item -> !item.getKey().after(date))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).entrySet().iterator();
		
		ILocation<Date> reference = person.get(date);
		while( iterator.hasNext() ) {
			Map.Entry<Date, IPerson<Date>> entry = iterator.next();
			ILocation<Date> check = entry.getValue().get( date );
			IContagion<Date>[] contagion = reference.getWorse(check);
			if( !Utils.assertNull(contagion))
				entry.getValue().alert(date, check);
		}			
		return true;
	}

	/**
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	@Override
	public ILocation<Date> createSnapShot() {
		Iterator<Map.Entry<Date, IPerson<Date>>> iterator = super.getPersons().entrySet().iterator();
		ILocation<Date> result = new DateLocation( super.getLocation().getIdentifier(), this );
		while( iterator.hasNext()) {
			Map.Entry<Date, IPerson<Date>> entry = iterator.next();
			ILocation<Date> snapshot = entry.getValue().createSnapshot();
			result = result.createWorst(snapshot);
		}
		return result;
	}

	@Override
	public ILocation<Date> update( Date current ) {	
		super.setLocation( createSnapShot());
		ILocation<Date> location = super.getLocation();
		int days = (int) (2 * DateLocation.getMaxContagionTime(location));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(current);
		calendar.add(Calendar.DAY_OF_YEAR, -days);
		super.getPersons().entrySet().removeIf(entry -> entry.getKey().before(calendar.getTime()));
		return location;
	}

	@Override
	public DateHub clone(){
		DateHub hub = new DateHub( super.getLocation() );
		Iterator<Map.Entry<Date, IPerson<Date>>> iterator = super.getPersons().entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, IPerson<Date>> entry = iterator.next();
			hub.getPersons().put(entry.getKey(), entry.getValue());
		}
		return hub;
	}	
}