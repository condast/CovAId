package org.covaid.core.def;

import java.util.Map;

public interface IPerson<T extends Object> extends Comparable<IPerson<T>>{

	int DEFAULT_ILL_THRESHOLD = 75;//%

	public enum States{
		HEALTHY,
		FEEL_ILL,
		APPOINTMENT,
		CONFIRMATION;
	}

	String getIdentifier();

	void setPosition(int xpos, int ypos);

	IPoint getLocation();

	States getState();

	void setState(States state);

	IContagion getMonitor();

	T getCurrent();

	boolean isHealthy();

	void setContagion(T current, T moment, IContagion contagion);

	void setIll(T date);

	void setIll(T date, String identifier);

	/**
	 * alert possible listeners of a new contagion that was recorded at the given moment
	 * @param step
	 * @param location
	 * @param contagion
	 */
	void alert(T current, T moment, IPoint location, IContagion contagion);
	
	/**
	 * Create a snapshot of the current risk of contagiousness
	 * @return
	 */
	ILocation<T> createSnapshot();

	/**
	 * Get the contagiousness at the given time step
	 * @param contagion
	 * @param step
	 * @return
	 */
	double getContagiousness(IContagion contagion, T step);

	double getContagiousness(IContagion contagion, T date, Map.Entry<T, ILocation<T>> entry);

	/**
	 * This is the measure in which you want to protect others
	 * @return
	 */
	double getSafetyBubble(IContagion contagion, T date);

	/**
	 * This is the measure in which are willing to take a risk
	 * @return
	 */
	double getRiskBubble(IContagion contagion);

	/**
	 * Update the person in normal circumstances
	 * @param date
	 */
	void updatePerson(T date);

	int compareTo(IPerson<T> o);

	ILocation<T> get(T date);

	void move(IPoint point);

	void addListener(IPersonListener<T> listener);

	void removeListener(IPersonListener<T> listener);
}