package org.covaid.core.hub;

import java.util.Map;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;

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
	boolean encounter(IPerson<T> person, T step, IContagion contagion);

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

	boolean isHealthy(IContagion contagion);

	double getContagion(IContagion contagion, T step);

	/**
	 * The long term prediction of the 
	 * @param contagion
	 * @return
	 */
	Map<T, Double> getPrediction(IContagion contagion, T range);

	/**
	 * Create a prediction of the risk of infection by comparing the hubs with the traces 
	 * @param hubs
	 * @return
	 */
	Map<T, Double> getPrediction(IContagion contagion, T range, Map<IPoint, ? extends IHub<T>> hubs);

	/**
	 * This trace has an increased risk of contagion. The guest should be a possible origin 
	 */
	void updateTrace(IContagion contagion, T current, IHub<T> guest);

	String printTrace();

	Map<T, Double> getTraces(IContagion contagion, T range);

	void enableTrace(boolean enabled);

	boolean isTraceEnabled();
}