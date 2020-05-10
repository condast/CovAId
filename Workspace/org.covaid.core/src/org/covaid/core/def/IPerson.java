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

	IContagion<T> getMonitor();

	T getCurrent();

	boolean isHealthy();

	void setContagion(T date, IContagion<T> contagion);

	void setIll(T date);

	void setIll(T date, String identifier);

	IHistory<T> getHistory();

	void alert(T date, ILocation<T> point);

	/**
	 * Create a snapshot of the current risk of contagiousness
	 * @return
	 */
	ILocation<T> createSnapshot();

	double getContagiousness(IContagion<T> contagion);

	double getContagiousness(IContagion<T> contagion, T date, Map.Entry<T, ILocation<T>> entry);

	/**
	 * This is the measure in which you want to protect others
	 * @return
	 */
	double getSafetyBubble(IContagion<T> contagion, T date);

	/**
	 * This is the measure in which are willing to take a risk
	 * @return
	 */
	double getRiskBubble(IContagion<T> contagion);

	/**
	 * Update the person in normal circumstances
	 * @param date
	 */
	void updatePerson(T date);

	int compareTo(IPerson<T> o);

	ILocation<T> get(T date);

	void move(IPoint point);

}