package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.condast.commons.data.util.Vector;
import org.condast.commons.date.DateUtils;
import org.covaid.core.def.HistoryEvent;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistory;
import org.covaid.core.def.IHistoryListener;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class History implements IHistory {

	private Map<Date, ILocation> history;

	private Date current;
	
	private Collection<IHistoryListener> listeners;
	
	public History() {
		this.history = new ConcurrentHashMap<Date, ILocation> ( new TreeMap<>());
		this.listeners = new ArrayList<>();
	}

	@Override
	public void addListener( IHistoryListener listener ) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener( IHistoryListener listener ) {
		this.listeners.remove(listener);
	}

	protected void notifyListeners( HistoryEvent event ) {
		for( IHistoryListener listener: this.listeners )
			listener.notifyContagionChanged(event);
	}

	/**
	 * Alert of a new contagion. 
	 * @param date
	 * @param location
	 */
	@Override
	public void alert( Date date, IPoint location, IContagion contagion ) {
		Location loc = new Location( location );
		loc.addContagion(contagion);
		putHistory( date, loc);
		notifyListeners( new HistoryEvent( this, date, location, contagion ));
	}

	/**
	 * Alert of a new contagion. 
	 * @param date
	 * @param location
	 */
	@Override
	public void putHistory( Date date, ILocation location ) {
		current = date;
		update( date, location );
		this.history.put( date, location);
	}


	@Override
	public Map<Date, ILocation> get() {
		return this.history;
	}

	@Override
	public ILocation get( Date date ) {
		return this.history.get(date);
	}

	
	@Override
	public Date getCurrent() {
		return current;
	}
	
	@Override
	public boolean isEmpty() {
		return this.history.isEmpty();
	}
	
	/**
	 * Get the most recent history object
	 * @return
	 */
	@Override
	public ILocation getRecent(){
		return this.history.get(current);
	}
	
	/**
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	@Override
	public Location createSnapShot( Date date, IPoint point ) {
		Location current = new Location( point );
		Iterator<Map.Entry<Date, ILocation>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation> entry = iterator.next();
			long days = DateUtils.getDifferenceDays( entry.getKey(), date );			
			for( IContagion test: entry.getValue().getContagion()) {
				double risk = test.getContagiousnessInTime(days);
				double reference = current.getContagion(test);
				if( reference < risk )
					current.addContagion( new Contagion(test.getIdentifier(), risk ));
			}
		}
		return current;
	}
	
	@Override
	public boolean isContagious( long days ) {
		Iterator<Map.Entry<Date, ILocation>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation> entry = iterator.next();
			if( !entry.getValue().isContagious( days ))
				this.history.remove(entry.getKey());
			else
				return true;
		}
		return false;
	}

	@Override
	public boolean isContagious( Date date ) {
		Iterator<Map.Entry<Date, ILocation>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation> entry = iterator.next();
			if( entry.getValue().isContagious( date ))
				return true;
			this.history.remove(entry.getKey());
		}
		return false;
	}

	/**
	 * Get the history with the maximum contagiousness
	 * @param contagion
	 * @return
	 */
	@Override
	public IContagion getMonitor() {
		Iterator<Map.Entry<Date, ILocation>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		IContagion monitor = null;
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation> entry = iterator.next();
			for( IContagion cont: entry.getValue().getContagion() ) {
				if( cont.getContagiousness() > probability)
					monitor = cont;
			}
		}
		return monitor;
	}

	/**
	 * Get the history with the maximum contagiousness
	 * @param contagion
	 * @return
	 */
	@Override
	public Vector<Date, ILocation> getMaxContagiousness( IContagion contagion ) {
		Iterator<Map.Entry<Date, ILocation>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		Vector<Date, ILocation> result = null;
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation> entry = iterator.next();
			double cont = entry.getValue().getContagion( contagion );
			if( cont > probability) {
				result = new Vector<Date, ILocation>( entry.getKey(), entry.getValue() );
			}
		}
		return result;
	}

	@Override
	public void clean( Date date ) {
		Iterator<Map.Entry<Date, ILocation>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation> entry = iterator.next();
			if( !entry.getValue().isContagious( date ))
				this.history.remove(entry.getKey());
		}
	}

	/**
	 * Update the history. Returns true if the contagion has gotten worse
	 * @param date
	 * @param location
	 */
	@Override
	public boolean update( Date date, ILocation location ) {
		ILocation previous = this.history.get(date);
		boolean result = false;
		if( previous == null ) {
			previous = location;
			result = true;
		}else if( location.isWorse(previous)){
			previous = previous.createWorst(location);
			result = true;
		}
		this.history.put(date, previous);		
		return result;
	}


	/**
	 * Calculate the maximum contagion of the given test object for the reference
	 * @param contagion
	 * @param reference
	 * @param test
	 * @return
	 */
	public static double getContagion( IContagion contagion, IPoint location, Date date, Map.Entry<Date, Location> test) {
		long days = DateUtils.getDifferenceDays( date, test.getKey());
		double distance = location.getDistance( test.getValue());
		return contagion.getContagiousness( days, distance );
	}
}
