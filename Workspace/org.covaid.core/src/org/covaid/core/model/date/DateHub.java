package org.covaid.core.model.date;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.condast.commons.Utils;
import org.condast.commons.date.DateUtils;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHub;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.model.Point;

public class DateHub extends Point implements IHub<Date> {

	//A list of person identifiers and when they were present here
	private Map<Date, IPerson<Date>> persons;
	
	private ILocation<Date> location;

	public DateHub(String identifier, int xpos, int ypos) {
		this(new DateLocation( identifier, xpos, ypos ));
	}

	public DateHub( ILocation<Date> location) {
		super(location.getIdentifier(), location.getXpos(), location.getYpos());
		this.persons = new TreeMap<>();
		this.location = location;
	}

	/**
	 * convenience method to 
	 * @param person
	 */
	public DateHub( IPerson<Date> person ) {
		this( person.getLocation().getIdentifier(), person.getLocation().getXpos(), person.getLocation().getYpos());
	}
	
	@Override
	public ILocation<Date> getLocation() {
		return location;
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

		//Determine the worst case situation for the encounter
		ILocation<Date> worst = DateLocation.createWorseCase(this.location, check);
		
		//Check if the person is worse off, and if so generate an alert		
		IContagion<Date>[] worse = check.getWorse( worst );
		if( Utils.assertNull(worse)) {
			person.alert( date, worst);
		}
		
		//If the hub has deteriorated, then add the person 
		worse = this.location.getWorse( worst );
		if( Utils.assertNull(worse))
			this.location = worst;
			
		persons.put( date, person );
		return true;
	}

	public boolean isEmpty() {
		return this.persons.isEmpty();
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
		
		this.location = createSnapShot(date);
		Iterator<Map.Entry<Date, IPerson<Date>>> iterator = this.persons.entrySet().stream()
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
	public ILocation<Date> createSnapShot( Date current ) {
		Iterator<Map.Entry<Date, IPerson<Date>>> iterator = this.persons.entrySet().iterator();
		ILocation<Date> result = new DateLocation( this.location.getIdentifier(), this );
		while( iterator.hasNext()) {
			Map.Entry<Date, IPerson<Date>> entry = iterator.next();
			long days = DateUtils.getDifferenceDays( current, entry.getKey());
			ILocation<Date> snapshot = entry.getValue().getHistory().createSnapShot(current, this);
			for( IContagion<Date> test: snapshot.getContagion()) {
				double risk = test.getContagiousnessInTime(days);
				double reference = result.getContagion(test);
				if( reference < risk )
					result.addContagion( new DateContagion(test.getIdentifier(), risk ));
			}
		}
		return result;
	}

	@Override
	public ILocation<Date> update( Date current ) {	
		this.location = createSnapShot(current);
		int days = (int) (2 * DateLocation.getMaxContagionTime(location));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(current);
		calendar.add(Calendar.DAY_OF_YEAR, -days);
		this.persons.entrySet().removeIf(entry -> entry.getKey().before(calendar.getTime()));
		return location;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		DateHub hub = new DateHub( location );
		Iterator<Map.Entry<Date, IPerson<Date>>> iterator = persons.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, IPerson<Date>> entry = iterator.next();
			hub.persons.put(entry.getKey(), entry.getValue());
		}
		return hub;
	}

	@Override
	public Map<Date, IPerson<Date>> getPersons() {
		return this.persons;
	}
}