package org.covaid.core.def;

import java.util.Date;

import org.covaid.core.data.SharedData;

public interface IMobile {

	public enum Risks{
		DONT_CARE(0),
		ADVENTUROUS(80),
		AVERAGE(50),
		PLAY_SAFE(20),
		NO_RISC(100);
		
		private int index;
		
		private Risks(int index ) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}	
	}

	String getIdentifier();

	double getHealth();

	void setHealth(double risk);

	double getSafety();

	void setSafety(double safety);

	IPoint getLocation();

	void setLocation(IPoint location);

	void alert(Date date, ILocation location, IContagion contagion);

	IHistory getHistory();

	/**
	 * Returns true if the risk assessment shows that the owner is healthy
	 * @return
	 */
	boolean isHealthy();

	boolean addContact(SharedData data);

	void setIdentifier(String identifier);

	Date getTimestamp();

	String getEmail();

	void setEmail(String email);

}