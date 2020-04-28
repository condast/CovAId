package org.covaid.core.def;

import java.util.Date;
import java.util.Map;

public interface IPerson extends Comparable<IPerson>{

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

	Date getCurrent();

	boolean isHealthy();

	void setContagion(Date date, IContagion contagion);

	void setIll(Date date);

	void setIll(Date date, String identifier);

	IHistory getHistory();

	void alert(Date date, ILocation point);

	/**
	 * Create a snapshot of the current risk of contagiousness
	 * @return
	 */
	ILocation createSnapshot();

	double getContagiousness(IContagion contagion);

	double getContagiousness(IContagion contagion, Date date, Map.Entry<Date, ILocation> entry);

	/**
	 * This is the measure in which you want to protect others
	 * @return
	 */
	double getSafetyBubble(IContagion contagion, Date date);

	/**
	 * This is the measure in which are willing to take a risk
	 * @return
	 */
	double getRiskBubble(IContagion contagion);

	/**
	 * Update the person in normal circumstances
	 * @param date
	 */
	void updatePerson(Date date);

	int compareTo(IPerson o);

}