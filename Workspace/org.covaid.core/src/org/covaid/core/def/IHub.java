package org.covaid.core.def;

import java.util.Date;

public interface IHub {

	/**
	 * Respond to an encounter with a person
	 * @param person
	 * @return
	 */
	boolean encounter(IPerson person, Date date);

	/**
	 * Respond to an encounter with a person. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	boolean alert(IPerson person, Date date);

	/**
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	ILocation createSnapShot(Date date );

	/**
	 * Update the hub and return a new snapshot
	 * @param date
	 * @return
	 */
	ILocation update(Date date);

	ILocation getLocation();

}