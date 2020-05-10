package org.covaid.core.def;

import java.util.Map;

public interface IHub<T extends Object> extends Cloneable{

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
	ILocation<T> createSnapShot(T step );

	/**
	 * Update the hub and return a new snapshot
	 * @param date
	 * @return
	 */
	ILocation<T> update(T step);

	ILocation<T> getLocation();

	Map<T, IPerson<T>> getPersons();

}