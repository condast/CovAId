package org.covaid.core.def;

import org.covaid.core.data.frogger.LocationData;
import org.covaid.core.operators.IContagionOperator;

public interface ILocation<T extends Object> extends IPoint{

	/**
	 * Add a contagion and the moment it is signalled
	 * @param timestamp
	 * @param contagion
	 * @param contagiousness
	 */
	void addContagion(T timestamp, IContagion contagion);

	/**
	 * Add a contagion if it is less than 100%
	 * @param timestamp
	 * @param contagion
	 * @param risk
	 */
	boolean addContagion(T timestamp, IContagion contagion, double risk);

	boolean removeContagion(IContagion contagion);

	double getContagion( IContagion contagion, T step );

	IContagion[] getContagion();

	boolean isHealthy( T step );

	boolean isHealthy( IContagion contagion, T step );

	boolean isContagious(T step );

	/**
	 * Return the list of contagions that are worse in the given location.
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	IContagion[] getWorse( ILocation<T> location);

	IPoint toPoint();

	/**
	 * Returns the worst possible situation when combining the contagiousness of both locations
	 * Returns null if the locations are not the same
	 * @param location
	 * @return
	 */
	ILocation<T> createWorst(ILocation<T> location);

	/**
	 * Return true if the given location is more contagious than this one. This
	 * means that there is at least one more contagious infection
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	boolean isWorse(ILocation<T> location);

	/**
	 * Get the date of this infection
	 * @param contagion
	 * @return
	 */
	T getInfectionDate(IContagion contagion);

	/**
	 * Returns true if this location is infected
	 * @param contagion
	 * @return
	 */
	boolean isInfected(IContagion contagion, T step);

	@Override
	ILocation<T> clone();

	/**
	 * move this location to the given point
	 * @param point
	 * @return
	 */
	void move(IPoint point);

	/**
	 * Get the risk of being infected by the given contagion at the given time
	 * @param contagion
	 * @param step
	 * @return
	 */
	double getRisk(IContagion contagion, T step);	

	/**
	 * Set the current risk of the given contagion. 
	 * @param contagion
	 * @param step; the moment that the contagion occurs
	 * @param risk
	 * @return
	 */
	boolean setRisk(IContagion contagion, T step, double risk);

	LocationData<T> toLocationData();

	IContagionOperator<T> getOperator();
}