package org.covaid.core.hub;

import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;

public interface IHub<T extends Object> extends Cloneable{

	public static final int DEFAULT_HISTORY = 60;//two months
	
	/**
	 * Respond to an encounter with a person
	 * @param person
	 * @return
	 */
	boolean encounter(IPerson<T> person, T step);

	/**
	 * Respond to an encounter with a person. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	boolean alert(IPerson<T> person, T step);

	/**
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	ILocation<T> createSnapShot();

	/**
	 * Update the hub and return a new snapshot
	 * @param date
	 * @return
	 */
	ILocation<T> update( T timeStep );

	ILocation<T> getLocation();

	Map<IPerson<T>,T> getPersons();

	boolean addPrevious(IHub<T> point);

	boolean removePrevious(IPoint point);

	IPoint[] getPrevious();

	void addListener(IHubListener<T> listener);

	void removeListener(IHubListener<T> listener);

	boolean isHealthy(IContagion<T> contagion);

	double getContagion(IContagion<T> contagion, T step);
}