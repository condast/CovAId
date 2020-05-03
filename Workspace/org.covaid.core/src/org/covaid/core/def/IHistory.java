package org.covaid.core.def;

import java.util.Date;
import java.util.Map;

import org.condast.commons.data.util.Vector;

public interface IHistory {

	void addListener(IHistoryListener listener);

	void removeListener(IHistoryListener listener);

	/**
	 * Alert of a new contagion. 
	 * @param date
	 * @param location
	 */
	void alert(Date date, IPoint location, IContagion contagion);

	/**
	 * Alert of a new contagion. 
	 * @param date
	 * @param location
	 */
	void putHistory(Date date, ILocation location);

	Map<Date, ILocation> get();

	Date getCurrent();

	boolean isEmpty();

	/**
	 * Get the most recent history object
	 * @return
	 */
	ILocation getRecent();

	/**
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	ILocation createSnapShot(Date date, IPoint point);

	boolean isContagious(long days);

	boolean isContagious(Date date);

	/**
	 * Get the history with the maximum contagiousness
	 * @param contagion
	 * @return
	 */
	IContagion getMonitor();

	/**
	 * Get the history with the maximum contagiousness
	 * @param contagion
	 * @return
	 */
	Vector<Date, ILocation> getMaxContagiousness(IContagion contagion);

	void clean(Date date);

	boolean update(Date date, ILocation location);

	ILocation get(Date date);

}