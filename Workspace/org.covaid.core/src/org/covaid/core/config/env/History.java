package org.covaid.core.config.env;

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

	public void put( Date date, Location location ) {
		update( date, location);
		this.history.put( date, location);
		current = date;
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
	
	public void update( Date date, Location location ) {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		boolean result = false;
		while( iterator.hasNext() ) {
			Map.Entry<Date, Location> entry = iterator.next();
			result = entry.getValue().updateContagion(date, location);
			if( result )
				notifyListeners( new HistoryEvent( this, entry.getKey(), entry.getValue()));
		}
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
	public Entry<Date, Location> getMaxContagiousness( Contagion contagion ) {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		Entry<Date, Location> result = null;
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
			Contagion cont = entry.getValue().getContagion( contagion.getIdentifier() );
			double test = ( cont == null )?0: cont.getContagiousness(); 
			if( test > probability) {
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
