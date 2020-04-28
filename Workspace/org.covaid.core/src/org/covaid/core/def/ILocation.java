package org.covaid.core.def;

import java.util.Date;

public interface ILocation extends IPoint{

	void addContagion( IContagion contagion);

	boolean removeContagion(IContagion contagion);

	double getContagion(IContagion contagion);

	IContagion[] getContagion();

	boolean isContagious(Date date);

	boolean isContagious(long days);

	boolean updateContagion( IContagion contagion);

	/**
	 * Return the list of contagions that are worse in the given location.
	 * Returns an empty list of the locations are not the same
	 * @param location
	 * @return
	 */
	IContagion[] isWorse( ILocation location);

	IPoint toPoint();

}