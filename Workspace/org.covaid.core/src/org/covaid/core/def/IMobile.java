package org.covaid.core.def;

public interface IMobile<T extends Object> {

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

	double getRisk();

	void setRisk(double safety);

	IPoint getLocation();

	void setLocation(IPoint location);

	void alert(T date, ILocation<T> location, IContagion<T> contagion);

	IHistory<T> getHistory();

	/**
	 * Returns true if the risk assessment shows that the owner is healthy
	 * @return
	 */
	boolean isHealthy();

	void setIdentifier(String identifier);

	T getTimestamp();

	String getEmail();

	void setEmail(String email);

	/**
	 * Set the health and safety based on a questionnaire
	 * @param cough
	 * @param fever
	 * @param lackoftaste
	 * @param soreThroat
	 * @param nasalCold
	 * @param temperature
	 * @return
	 */
	double getHealthAdvice(boolean cough, boolean fever, boolean lackoftaste, boolean soreThroat, boolean nasalCold,
			double temperature);

}