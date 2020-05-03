package org.covaid.core.def;

import java.util.Date;

import org.condast.commons.data.plane.IField;
import org.covaid.core.environment.AbstractDomain;

public interface IEnvironment {

	public enum Events{
		ACTIVITY,
		HOUR,
		NEW_DAY;
	}
	
	int DEFAULT_POPULATION = 100000;//is just under 1 person for every 10*10 metres 
	int MILLION = 1000000;
	int DEFAULT_ACTIVITY = 60 * 24;//minutes
	int DEFAULT_RADIUS = 10;//metres, the maximum movement that a person can make during one step in activity
	int DEFAULT_SPEED = 20;//metres, the maximum movement that a person can make during one step in activity

	void init(int population);

	/**
	 * Population is amount of people per square kilometre(!)
	 * @param population
	 */
	void init(int activity, int population);

	String getDayString(boolean trunc);

	String getContagion();

	void setContagion(String contagion);

	IField getField();

	void setField(IField field);

	void zoomIn();

	void zoomOut();

	int getDays();

	/**
	 * Get the simulated date
	 * @return
	 */
	Date getDate();

	void start();

	void stop();

	void clear();

	void dispose();

	String getName();

	int getPopulation();

	void addDomain(AbstractDomain domain);

	void removeDomain(AbstractDomain domain);

	AbstractDomain[] getDomains();

	void addListener(IEnvironmentListener listener);

	void removeListener(IEnvironmentListener listener);
}