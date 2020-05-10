package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.condast.commons.data.util.Vector;
import org.covaid.core.def.HistoryEvent;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistory;
import org.covaid.core.def.IHistoryListener;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPoint;

import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractHistory<T extends Object> implements IHistory<T> {

	private Map<T, ILocation<T>> history;

	private T current;
	
	private Collection<IHistoryListener<T>> listeners;
	
	protected AbstractHistory() {
		this.history = new ConcurrentHashMap<T, ILocation<T>> ( new TreeMap<>());
		this.listeners = new ArrayList<>();
	}

	@Override
	public void addListener( IHistoryListener<T> listener ) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener( IHistoryListener<T> listener ) {
		this.listeners.remove(listener);
	}

	protected void notifyListeners( HistoryEvent<T> event ) {
		for( IHistoryListener<T> listener: this.listeners )
			listener.notifyContagionChanged(event);
	}

	/**
	 * Alert of a new contagion. 
	 * @param date
	 * @param location
	 */
	@Override
	public void alert( T date, IPoint location, IContagion<T> contagion ) {
		ILocation<T> loc = createLocation(location.getIdentifier(), location );
		loc.addContagion(contagion);
		putHistory( date, loc);
		notifyListeners( new HistoryEvent<T>( this, date, location, contagion ));
	}

	/**
	 * Alert of a new contagion. 
	 * @param date
	 * @param location
	 */
	@Override
	public void putHistory( T date, ILocation<T> location ) {
		current = date;
		update( date, location );
		this.history.put( date, location);
	}


	@Override
	public Map<T, ILocation<T>> get() {
		return this.history;
	}

	@Override
	public ILocation<T> get( T date ) {
		return this.history.get(date);
	}

	
	@Override
	public T getCurrent() {
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
	public ILocation<T> getRecent(){
		return this.history.get(current);
	}
	
	protected abstract long getDifference( T first, T last );

	protected abstract IContagion<T> createContagion( String identifier, double safety );

	protected abstract ILocation<T> createLocation( String identifier, IPoint point );

	/**
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	@Override
	public ILocation<T> createSnapShot( T step, IPoint point ) {
		ILocation<T> current = createLocation( point.toString(), point);
		Iterator<Map.Entry<T, ILocation<T>>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<T, ILocation<T>> entry = iterator.next();
			long days = getDifference(entry.getKey(), step );			
			for( IContagion<T> test: entry.getValue().getContagion()) {
				double risk = test.getContagiousnessInTime(days);
				double reference = current.getContagion(test);
				if( reference < risk )
					current.addContagion( this.createContagion(test.getIdentifier(), risk ));
			}
		}
		return current;
	}
	
	@Override
	public boolean isContagious( long days ) {
		Iterator<Map.Entry<T, ILocation<T>>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<T, ILocation<T>> entry = iterator.next();
			if( !entry.getValue().isContagious( days ))
				this.history.remove(entry.getKey());
			else
				return true;
		}
		return false;
	}

	@Override
	public boolean isContagious( T date ) {
		Iterator<Map.Entry<T, ILocation<T>>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<T, ILocation<T>> entry = iterator.next();
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
	public IContagion<T> getMonitor() {
		Iterator<Map.Entry<T, ILocation<T>>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		IContagion<T> monitor = null;
		while( iterator.hasNext()) {
			Map.Entry<T, ILocation<T>> entry = iterator.next();
			for( IContagion<T> cont: entry.getValue().getContagion() ) {
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
	public Vector<T, ILocation<T>> getMaxContagiousness( IContagion<T> contagion ) {
		Iterator<Map.Entry<T, ILocation<T>>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		Vector<T, ILocation<T>> result = null;
		while( iterator.hasNext()) {
			Map.Entry<T, ILocation<T>> entry = iterator.next();
			double cont = entry.getValue().getContagion( contagion );
			if( cont > probability) {
				result = new Vector<T, ILocation<T>>( entry.getKey(), entry.getValue() );
			}
		}
		return result;
	}

	@Override
	public void clean( T date ) {
		Iterator<Map.Entry<T, ILocation<T>>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<T, ILocation<T>> entry = iterator.next();
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
	public boolean update( T date, ILocation<T> location ) {
		ILocation<T> previous = this.history.get(date);
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
}
