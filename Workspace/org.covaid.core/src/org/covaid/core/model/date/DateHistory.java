package org.covaid.core.model.date;

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

public class DateHistory implements IHistory<Date> {

	private Map<Date, ILocation<Date>> history;

	private Date current;
	
	private Collection<IHistoryListener<Date>> listeners;
	
	public DateHistory() {
		this.history = new ConcurrentHashMap<Date, ILocation<Date>> ( new TreeMap<>());
		this.listeners = new ArrayList<>();
	}

	@Override
	public void addListener( IHistoryListener<Date> listener ) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener( IHistoryListener<Date> listener ) {
		this.listeners.remove(listener);
	}

	protected void notifyListeners( HistoryEvent<Date> event ) {
		for( IHistoryListener<Date> listener: this.listeners )
			listener.notifyContagionChanged(event);
	}

	/**
	 * Alert of a new contagion. 
	 * @param date
	 * @param location
	 */
	@Override
	public void alert( Date date, IPoint location, IContagion<Date> contagion ) {
		DateLocation loc = new DateLocation( location );
		loc.addContagion(contagion);
		putHistory( date, loc);
		notifyListeners( new HistoryEvent<Date>( this, date, location, contagion ));
	}

	/**
	 * Alert of a new contagion. 
	 * @param date
	 * @param location
	 */
	@Override
	public void putHistory( Date date, ILocation<Date> location ) {
		current = date;
		update( date, location );
		this.history.put( date, location);
	}


	@Override
	public Map<Date, ILocation<Date>> get() {
		return this.history;
	}

	@Override
	public ILocation<Date> get( Date date ) {
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
	public ILocation<Date> getRecent(){
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
	public DateLocation createSnapShot( Date date, IPoint point ) {
		DateLocation current = new DateLocation( point );
		Iterator<Map.Entry<Date, ILocation<Date>>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation<Date>> entry = iterator.next();
			long days = DateUtils.getDifferenceDays( entry.getKey(), date );			
			for( IContagion<Date> test: entry.getValue().getContagion()) {
				double risk = test.getContagiousnessInTime(days);
				double reference = current.getContagion(test);
				if( reference < risk )
					current.addContagion( new DateContagion(test.getIdentifier(), risk ));
			}
		}
		return current;
	}
	
	@Override
	public boolean isContagious( long days ) {
		Iterator<Map.Entry<Date, ILocation<Date>>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation<Date>> entry = iterator.next();
			if( !entry.getValue().isContagious( days ))
				this.history.remove(entry.getKey());
			else
				return true;
		}
		return false;
	}

	@Override
	public boolean isContagious( Date date ) {
		Iterator<Map.Entry<Date, ILocation<Date>>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation<Date>> entry = iterator.next();
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
	public IContagion<Date> getMonitor() {
		Iterator<Map.Entry<Date, ILocation<Date>>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		IContagion<Date> monitor = null;
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation<Date>> entry = iterator.next();
			for( IContagion<Date> cont: entry.getValue().getContagion() ) {
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
	public Vector<Date, ILocation<Date>> getMaxContagiousness( IContagion<Date> contagion ) {
		Iterator<Map.Entry<Date, ILocation<Date>>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		Vector<Date, ILocation<Date>> result = null;
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation<Date>> entry = iterator.next();
			double cont = entry.getValue().getContagion( contagion );
			if( cont > probability) {
				result = new Vector<Date, ILocation<Date>>( entry.getKey(), entry.getValue() );
			}
		}
		return result;
	}

	@Override
	public void clean( Date date ) {
		Iterator<Map.Entry<Date, ILocation<Date>>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, ILocation<Date>> entry = iterator.next();
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
	public boolean update( Date date, ILocation<Date> location ) {
		ILocation<Date> previous = this.history.get(date);
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
	public static double getContagion( IContagion<Date> contagion, IPoint location, Date date, Map.Entry<Date, DateLocation> test) {
		long days = DateUtils.getDifferenceDays( date, test.getKey());
		double distance = location.getDistance( test.getValue());
		return contagion.getContagiousness( days, distance );
	}
}
