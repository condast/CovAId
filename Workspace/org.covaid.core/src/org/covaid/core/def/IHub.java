package org.covaid.core.def;

import java.util.Date;

import org.covaid.core.model.Location;

public interface IHub {

	/**
	 * Respond to an encounter with a person
	 * @param person
	 * @return
	 */
	boolean encounter(Date date, IPerson person);

	/**
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	ILocation createSnapshot(Date date);

	/**
	 * Create a new location from the reference by adding the highest contagions
	 * from loc2
	 * @param reference
	 * @param loc2
	 * @return
	 */
	Location createWorseCase(ILocation reference, ILocation loc2);

	void updateHub(Date date);

}