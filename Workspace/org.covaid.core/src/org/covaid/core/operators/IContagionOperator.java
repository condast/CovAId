package org.covaid.core.operators;

import org.condast.commons.data.util.Vector;
import org.covaid.core.data.ContagionData;
import org.covaid.core.def.IContagion;

public interface IContagionOperator<T extends Object> extends Comparable<T>{

	/**
	 * Get the difference between the first and the last time
	 * @param first
	 * @param last
	 * @return
	 */
	long getDifference(T first, T last);

	/**
	 * Returns true if the first is smaller than the last
	 * @param first
	 * @param last
	 * @return
	 */
	boolean isSmaller(T first, T last);

	/**
	 * return true if the reference is in between the from value and the current value 
	 * @param from
	 * @return
	 */
	public boolean isLastEntry( T from, T reference );

	/**
	 * result = first-last 
	 * @param from
	 * @return
	 */
	public T subtract( T first, T last );

	/**
	 * The current time
	 * @return
	 */
	T getCurrent();

	/**
	 * Set the current time
	 * @param init
	 */
	void setCurrent(T init);

	/**
	 * Get the contagion as it spreads in the seconds after contact
	 * <contagion, radius>
	 * 
	 * @param contagion
	 * @param current
	 * @param distance
	 * @return
	 */
	Vector<Double, Double> getContagion(T current);

	/**
	 * Get the contagion as it spreads in the seconds after contact
	 * <contagion, radius>
	 * 
	 * @param contagion
	 * @param step
	 * @param distance
	 * @return
	 */
	Vector<Double, Double> getContagionRange( T start);

	/**
	 * Returns true if the this contagiousness is larger than the given one
	 * @param contagion
	 * @return
	 */
	boolean isLarger(IContagion contagion);

	/**
	 * returns true if the given combination of the start of the infection and the risk
	 *  signifies a contagion
	 * @param init
	 * @param risk
	 * @return
	 */
	boolean isContagious(T start, double risk);

	/**
	 * returns true if the owner is contagious, according to the given data
	 * @param data
	 * @return
	 */
	boolean isContagious(ContagionData<T> data);

	double getContagiousness( T start );

	IContagion getContagion();

	void setContagion(IContagion contagion);

	/**
	 * Calculate the contagiousness, according to the provided data
	 * @param data
	 * @return
	 */
	double getContagiousness(ContagionData<T> data);

	boolean isInfected(ContagionData<T> data);

	boolean isHealthy(ContagionData<T> data);

	/**
	 * The transferred contagion is transfered from one hub to another
	 * @param data
	 * @return
	 */
	double getTransferContagiousness(ContagionData<T> data);
}