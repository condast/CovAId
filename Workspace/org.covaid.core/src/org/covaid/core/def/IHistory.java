package org.covaid.core.def;

import java.util.Map;

import org.condast.commons.data.util.Vector;

public interface IHistory<T extends Object> {

	void addListener(IHistoryListener<T> listener);

	void removeListener(IHistoryListener<T> listener);

	/**
	 * Alert of a new contagion. 
	 * @param date
	 * @param location
	 */
	void alert(T date, IPoint location, IContagion contagion, double conatagiousness);

	/**
	 * Alert of a new contagion. 
	 * @param date
	 * @param location
	 */
	void putHistory(T date, ILocation<T> location);

	Map<T, ILocation<T>> get();

	T getCurrent();

	boolean isEmpty();

	/**
	 * Get the most recent history object
	 * @return
	 */
	ILocation<T> getRecent();

	/**
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	ILocation<T> createSnapShot(IPoint point);

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
	Vector<T, ILocation<T>> getMaxContagiousness(IContagion contagion);

	void clean(T date);

	boolean update(T date, ILocation<T> location);

	ILocation<T> get(T date);

}