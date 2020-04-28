package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.condast.commons.Utils;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistory;
import org.covaid.core.def.IHistoryListener;
import org.covaid.core.def.IHub;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;

public class Hub extends Point implements IHub {

	private Collection<IHistory> histories;
	
	private IHistoryListener listener = (e)->{
		for( IHistory history: this.histories) {
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
	public Hub( IPerson person ) {
		this( person.getLocation().getIdentifier(), person.getLocation().getXpos(), person.getLocation().getYpos());
	}
	
	/**
	 * Respond to an encounter with a person
	 * @param person
	 * @return
	 */
	@Override
	public boolean encounter( Date date, IPerson person ) {
		if( person.isHealthy() || !person.getLocation().getIdentifier().equals( super.getIdentifier()))
			return false;
		ILocation reference = createSnapshot( person.getCurrent());
		ILocation test = person.createSnapshot();
		
		//Check if the current situation is worst that the person's
		ILocation worst = createWorseCase(test, reference);
		IContagion[] worse = reference.isWorse(test);
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
	@Override
	public ILocation createSnapshot( Date date ) {
		Location current = new Location( this );
		ILocation worst = null;
		for( IHistory history: this.histories ) {
			ILocation snapshot = history.createSnapShot(date, this);
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
	@Override
	public Location createWorseCase( ILocation reference, ILocation loc2 ) {
		Location worst = new Location( reference );
		for( IContagion contagion: reference.getContagion() ) {
			double test = loc2.getContagion(contagion);
			if( contagion.getContagiousness() < test)
				worst.addContagion(contagion);
		}
		return worst;
	}
	
	@Override
	public void updateHub( Date date ) {
		Collection<IHistory> temp = new ArrayList<IHistory>( this.histories);
		for( IHistory history: temp ) {
			history.update(date, this);
			if( history.isEmpty()) {
				history.removeListener(listener);
				this.histories.remove(history);
			}
		}
	}
}