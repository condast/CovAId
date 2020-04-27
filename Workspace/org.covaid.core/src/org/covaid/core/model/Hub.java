package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.condast.commons.Utils;

public class Hub extends Point {

	private Collection<History> histories;
	
	private IHistoryListener listener = (e)->{
		for( History history: this.histories) {
			if( history.equals(e.getSource()))
				continue;
			history.alert(e.getDate(), e.getLocation(), e.getContagion());
		}
	};
	
	public Hub(String identifier, int xpos, int ypos) {
		super(identifier, xpos, ypos);
		this.histories = new ArrayList<>();
	}

	/**
	 * convenience method to 
	 * @param person
	 */
	public Hub( Person person ) {
		this( person.getLocation().getIdentifier(), person.getLocation().getXpos(), person.getLocation().getYpos());
	}
	
	/**
	 * Respond to an encounter with a person
	 * @param person
	 * @return
	 */
	public boolean encounter( Date date, Person person ) {
		if( person.isHealthy() || !person.getLocation().getIdentifier().equals( super.getIdentifier()))
			return false;
		Location reference = createSnapshot( person.getCurrent());
		Location test = person.createSnapshot();
		
		//Check if the current situation is worst that the person's
		Location worst = createWorseCase(test, reference);
		Contagion[] worse = reference.isWorse(test);
		if( Utils.assertNull(worse)) {
			person.alert( date, worst);
			return false;
		}	
		histories.add( person.getHistory() );
		person.getHistory().addListener(listener);
		return true;
	}


	/**
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	public Location createSnapshot( Date date ) {
		Location current = new Location( this );
		Location worst = null;
		for( History history: this.histories ) {
			Location snapshot = history.createSnapShot(date, this);
			worst = createWorseCase(current, snapshot);
		}
		return worst;
	}

	/**
	 * Create a new location from the reference by adding the highest contagions
	 * from loc2
	 * @param reference
	 * @param loc2
	 * @return
	 */
	public Location createWorseCase( Location reference, Location loc2 ) {
		Location worst = new Location( reference );
		for( Contagion contagion: reference.getContagion() ) {
			double test = loc2.getContagion(contagion);
			if( contagion.getContagiousness() < test)
				worst.addContagion(contagion);
		}
		return worst;
	}
	
	public void updateHub( Date date ) {
		Collection<History> temp = new ArrayList<History>( this.histories);
		for( History history: temp ) {
			history.update(date, this);
			if( history.isEmpty()) {
				history.removeListener(listener);
				this.histories.remove(history);
			}
		}
	}
}