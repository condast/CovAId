package org.covaid.core.environment;

public interface IEnvironment<T extends Object> {

	public enum Events{
		ACTIVITY,
		HOUR,
		NEW_DAY;
	}
	
	int DEFAULT_POPULATION = 100000;//is just under 1 person for every 10*10 metres 
	int MILLION = 1000000;
	int DEFAULT_ACTIVITY = 60 * 24;//minutes
	int DEFAULT_RADIUS = 10;//metres, the maximum movement that a person can make during one step in activity
	int DEFAULT_SPEED = 1100;//1.1 seconds

	int DEFAULT_LENGTH  = 1000; //1 km
	int DEFAULT_WIDTH  = 1000; //1 km

	void init(int population);

	int getDays();

	void start();

	boolean pause();

	void stop();

	void clear();

	void dispose();

	String getName();

	int getPopulation();

	void addDomain( IDomain<T> domain);

	void removeDomain( IDomain<T> domain);

	IDomain<T>[] getDomains();

	void addListener(IEnvironmentListener<T> listener);

	void removeListener(IEnvironmentListener<T> listener);

	T getTimeStep( long days );
}