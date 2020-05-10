package org.covaid.core.def;

public interface ILocation<T extends Object> extends IPoint{

	void addContagion( IContagion<T> contagion);

	boolean removeContagion(IContagion<T> contagion);

	double getContagion(IContagion<T> contagion);

	IContagion<T> getContagion(String identifier);

	IContagion<T>[] getContagion();

	boolean isContagious(T date);

	boolean isContagious(long days);

	/**
	 * Return the list of contagions that are worse in the given location.
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	IContagion<T>[] getWorse( ILocation<T> location);

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

}