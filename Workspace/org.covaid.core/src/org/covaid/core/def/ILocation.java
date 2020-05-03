package org.covaid.core.def;

import java.util.Date;

public interface ILocation extends IPoint{

	void addContagion( IContagion contagion);

	boolean removeContagion(IContagion contagion);

	double getContagion(IContagion contagion);

	IContagion getContagion(String identifier);

	IContagion[] getContagion();

	boolean isContagious(Date date);

	boolean isContagious(long days);

	/**
	 * Return the list of contagions that are worse in the given location.
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	IContagion[] getWorse( ILocation location);

	IPoint toPoint();

	/**
	 * Returns the worst possible situation when combining the contagiousness of both locations
	 * Returns null if the locations are not the same
	 * @param location
	 * @return
	 */
	ILocation createWorst(ILocation location);

	/**
	 * Return true if the given location is more contagious than this one. This
	 * means that there is at least one more contagious infection
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	boolean isWorse(ILocation location);

}