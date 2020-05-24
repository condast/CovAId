package org.covaid.core.hub;

import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.hub.trace.ITrace;

public interface IHub<T extends Object> extends Cloneable{

	public static final int DEFAULT_HISTORY = 60;//two months

	/**
	 * Respond to an encounter with a person. This happens when a person enters the location of this hub
	 * The snapshots of the person and the location are compared, and the person is alerted
	 * if the risk of infection has increased. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	boolean encounter(IPerson<T> person, T step);

	/**
	 * Respond to an encounter with a person. This happens when a person enters the location of this hub
	 * The snapshots of the person and the location are compared, and the person is alerted
	 * if the risk of infection has increased. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	boolean encounter(IPerson<T> person, T step, IContagion<T> contagion);

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

	void updateTrace(IContagion<T> contagion, T timeStep, ITrace<T> guest);

	/**
	 * The long term prediction of the 
	 * @param contagion
	 * @return
	 */
	Map<T, Double> getPrediction(IContagion<T> contagion, T range);
}