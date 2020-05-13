package org.covaid.core.model;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.condast.commons.Utils;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.AbstractHub;

public class Hub extends AbstractHub<Integer> {

	
	public Hub(IPoint location) {
		super(new Location( location.getIdentifier(), location.getXpos(), location.getYpos()));
	}

	/**
	 * convenience method to 
	 * @param person
	 */
	public Hub( IPerson<Integer> person ) {
		this( new Location( person.getLocation().getXpos(), person.getLocation().getYpos()));
	}
	
	/**
	 * Respond to an encounter with a person. This happens when a person enters the location of this hub
	 * The snapshots of the person and the location are compared, and the person is alerted
	 * if the risk of infection has increased. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	@Override
	public boolean encounter( IPerson<Integer> person, Integer step ) {
		if( !person.getLocation().equals( super.getLocation()))
			return false;
		ILocation<Integer> check = person.createSnapshot();

		//Determine the worst case situation for the encounter
		ILocation<Integer> worst = check.createWorst( check );
		
		//Check if the person is worse off, and if so generate an alert		
		IContagion<Integer>[] worse = check.getWorse( worst );
		if( !Utils.assertNull(worse)) {
			person.alert( step, worst);
		}
		
		//If the hub has deteriorated, then add the person 
		worse = super.getLocation().getWorse( worst );
		if( !Utils.assertNull(worse))
			super.setLocation( worst );
			
		super.put( step, person );
		return true;
	}

	/**
	 * Respond to an alert of a person. This happens when the person has become infected, and is alerting previous locations 
	 * of the infection. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	@Override
	public boolean alert( IPerson<Integer> person, Integer step ) {
		if( person.isHealthy() || !person.getLocation().getIdentifier().equals( super.getIdentifier()))
			return false;
		
		super.setLocation( createSnapShot());
		Iterator<Map.Entry<Integer, IPerson<Integer>>> iterator = super.getPersons().entrySet().stream()
				.filter(item -> item.getKey()<=step)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).entrySet().iterator();
		
		ILocation<Integer> reference = person.get(step);
		while( iterator.hasNext() ) {
			Map.Entry<Integer, IPerson<Integer>> entry = iterator.next();
			ILocation<Integer> check = entry.getValue().get( step );
			IContagion<Integer>[] contagion = reference.getWorse(check);
			if( !Utils.assertNull(contagion))
				entry.getValue().alert(step, check);
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
	public ILocation<Integer> createSnapShot() {
		Iterator<Map.Entry<Integer, IPerson<Integer>>> iterator = super.getPersons().entrySet().iterator();
		ILocation<Integer> result = new Location( super.getLocation().getIdentifier(), this );
		while( iterator.hasNext()) {
			Map.Entry<Integer, IPerson<Integer>> entry = iterator.next();
			ILocation<Integer> snapshot = entry.getValue().createSnapshot();
			ILocation<Integer> test = result.createWorst(snapshot);
			if( test != null )
				result = test;
		}
		return result;
	}

	@Override
	public ILocation<Integer> update( Integer timeStep ) {	
		ILocation<Integer> location = createSnapShot();
		//int days = (int) (2 * Location.getMaxContagionTime( super.getLocation()));
		super.getPersons().entrySet().removeIf(entry -> entry.getKey() > DEFAULT_HISTORY );
		return location;
	}

	@Override
	public Hub clone(){
		Hub hub = new Hub( super.getLocation() );
		Iterator<Map.Entry<Integer, IPerson<Integer>>> iterator = super.getPersons().entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Integer, IPerson<Integer>> entry = iterator.next();
			hub.put(entry.getKey(), entry.getValue());
		}
		return hub;
	}
}