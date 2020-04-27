package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.condast.commons.date.DateUtils;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class History {

	private Map<Date, Location> history;

	private Date current;
	
	private Collection<IHistoryListener> listeners;
	
	public History() {
		this.history = new ConcurrentHashMap<Date, Location> ( new TreeMap<>());
		this.listeners = new ArrayList<>();
	}

	public void addListener( IHistoryListener listener ) {
		this.listeners.add(listener);
	}

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
	public void alert( Date date, Point location, Contagion contagion ) {
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
	public void putHistory( Date date, Location location ) {
		current = date;
		update( date, location );
		this.history.put( date, location);
	}


	public Map<Date,Location> get() {
		return this.history;
	}

	public Date getCurrent() {
		return current;
	}
	
	public boolean isEmpty() {
		return this.history.isEmpty();
	}
	
	/**
	 * Get the most recent history object
	 * @return
	 */
	public Location getRecent(){
		return this.history.get(current);
	}
	
	/**
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	public Location createSnapShot( Date date, Point point ) {
		Location current = new Location( point );
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
			long days = DateUtils.getDifferenceDays( date, entry.getKey());			
			for( Contagion test: entry.getValue().getContagion()) {
				double risk = test.getContagiousnessInTime(days);
				double reference = current.getContagion(test);
				if( reference < risk )
					current.addContagion( new Contagion(test.getIdentifier(), risk ));
			}
		}
		return current;
	}
	
	public boolean isContagious( long days ) {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
			if( !entry.getValue().isContagious( days ))
				this.history.remove(entry.getKey());
			else
				return true;
		}
		return false;
	}

	public boolean isContagious( Date date ) {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
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
	public Contagion getMonitor() {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		Contagion monitor = null;
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
			for( Contagion cont: entry.getValue().getContagion() ) {
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
	public Entry<Date, Location> getMaxContagiousness( Contagion contagion ) {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		Entry<Date, Location> result = null;
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
			double cont = entry.getValue().getContagion( contagion );
			if( cont > probability) {
				result = entry;
			}
		}
		return result;
	}

	public void clean( Date date ) {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
			if( !entry.getValue().isContagious( date ))
				this.history.remove(entry.getKey());
		}
	}

	/**
	 * Update the history. Returns true if the contagion has gotten worse
	 * @param date
	 * @param location
	 */
	public boolean update( Date date, Point location ) {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		boolean result = false;
		while( iterator.hasNext() ) {
			Map.Entry<Date, Location> entry = iterator.next();
			double distance = entry.getValue().getDistance(location);
			for( Contagion contagion: entry.getValue().getContagion() )
				result |= entry.getValue().updateContagion(contagion);
			if( result )
				notifyListeners( new HistoryEvent( this, entry.getKey(), entry.getValue() ));
		}
		return result;
	}


	/**
	 * Calculate the maximum contagion of the given test object for the reference
	 * @param contagion
	 * @param reference
	 * @param test
	 * @return
	 */
	public static double getContagion( Contagion contagion, Point location, Date date, Map.Entry<Date, Location> test) {
		long days = DateUtils.getDifferenceDays( date, test.getKey());
		double distance = location.getDistance( test.getValue());
		return contagion.getContagiousness( days, distance );
	}
}
