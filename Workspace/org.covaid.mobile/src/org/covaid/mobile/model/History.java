package org.covaid.mobile.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity(name="HISTORY")
public class History implements IHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@OneToMany( mappedBy="location", cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	private Map<Date, Location> history;

	@Basic(optional = false)
	@Column( nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date current;
	
	private transient Collection<IHistoryListener> listeners;
	
	public History() {
		this.history = new ConcurrentHashMap<Date, Location> ( new TreeMap<Date, Location>());
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
		this.history.put( date, (Location)location);
	}


	@Override
	public Map<Date, ILocation> get() {
		Map<Date,ILocation> result = new HashMap<Date,ILocation>( this.history );
		return result;
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
	@Override
	public ILocation createSnapShot( Date date, IPoint point ) {
		Location current = new Location( point );
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
			long days = DateUtils.getDifferenceDays( date, entry.getKey());			
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

	@Override
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
	@Override
	public IContagion getMonitor() {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		IContagion monitor = null;
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
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
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		double probability = 0;
		Vector<Date, ILocation> result = null;
		while( iterator.hasNext()) {
			Map.Entry<Date, Location> entry = iterator.next();
			double cont = entry.getValue().getContagion( contagion );
			if( cont > probability) {
				result = new Vector<Date, ILocation>( entry.getKey(), entry.getValue() );
			}
		}
		return result;
	}

	@Override
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
	public boolean update( Date date, IPoint location ) {
		Iterator<Map.Entry<Date, Location>> iterator = this.history.entrySet().iterator();
		boolean result = false;
		while( iterator.hasNext() ) {
			Map.Entry<Date, Location> entry = iterator.next();
			double distance = entry.getValue().getDistance(location);
			for( IContagion contagion: entry.getValue().getContagion() )
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
	public static double getContagion( IContagion contagion, Point location, Date date, Map.Entry<Date, Location> test) {
		long days = DateUtils.getDifferenceDays( date, test.getKey());
		double distance = location.getDistance( test.getValue());
		return contagion.getContagiousness( days, distance );
	}
}
