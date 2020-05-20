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
		this( location, 0, DEFAULT_HISTORY );
	}
	
	public Hub(IPoint location, int timeStep, int history) {
		super(new Location( location.getIdentifier(), location.getXpos(), location.getYpos()), timeStep, history );
	}

	/**
	 * convenience method to 
	 * @param person
	 */
	public Hub( IPerson<Integer> person ) {
		this( new Location( person.getLocation().getXpos(), person.getLocation().getYpos()));
	}

	/**
	 * convenience method to 
	 * @param person
	 */
	public Hub( IPerson<Integer> person, int timeStep, int history ) {
		this( person.getLocation(), 0, history );
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
		if( !Utils.assertNull(worse)) {
			super.setLocation( worst );
		}
			
		super.put( person, step );
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
		Iterator<Map.Entry<IPerson<Integer>, Integer>> iterator = super.getPersons().entrySet().stream()
				.filter(item -> item.getValue()<=step)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).entrySet().iterator();
		
		ILocation<Integer> reference = person.get(step);
		while( iterator.hasNext() ) {
			Map.Entry<IPerson<Integer>, Integer> entry = iterator.next();
			ILocation<Integer> check = entry.getKey().get( step );
			IContagion<Integer>[] contagion = reference.getWorse(check);
			if( !Utils.assertNull(contagion))
				entry.getKey().alert(step, check);
		}			
		return true;
	}
	
	@Override
	protected boolean onPersonAlert(IPerson<Integer> person, Integer moment, Integer timeStep, Integer history, Integer encountered) {
		return ( timeStep - moment )<history ;
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
		Iterator<Map.Entry<IPerson<Integer>, Integer>> iterator = super.getPersons().entrySet().iterator();
		ILocation<Integer> result = new Location( super.getLocation().getIdentifier(), this );
		while( iterator.hasNext()) {
			Map.Entry<IPerson<Integer>, Integer> entry = iterator.next();
			ILocation<Integer> snapshot = entry.getKey().createSnapshot();
			ILocation<Integer> test = result.createWorst(snapshot);
			if( test != null )
				result = test;
		}
		return result;
	}
	
	@Override
	protected boolean onRemovePersons(IPerson<Integer> person, Integer timeStep, Integer history, Integer encountered) {
		return ( timeStep - encountered ) > history;
	}

	@Override
	public Hub clone(){
		Hub hub = new Hub( super.getLocation() );
		Iterator<Map.Entry<IPerson<Integer>, Integer>> iterator = super.getPersons().entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<IPerson<Integer>, Integer> entry = iterator.next();
			hub.put(entry.getKey(), entry.getValue());
		}
		return hub;
	}
}