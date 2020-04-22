package org.covaid.core.config.env;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class History {

	private Map<Date, Location> history;

	private Collection<IHistoryListener> listeners;
	
	public History() {
		this.history = new TreeMap<>();
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

	public Map<Date,Location> get() {
		return this.history;
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

	public void clean( Date date ) {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
			if( !entry.getValue().isContagious( date ))
				this.history.remove(entry.getKey());
		}
	}

}
