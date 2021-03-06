package org.covaid.core.def;

import org.condast.commons.strings.StringStyler;
import org.condast.commons.strings.StringUtils;

public interface IContagion extends Comparable<IContagion>{

	public static final int DAY = 24 * 3600 * 1000;//msec
	public static final int DEFAULT_CONTAGION = 14;//two weeks
	public static final int DEFAULT_DISTANCE = 10;//10 meters
	public static final int DEFAULT_HALFTIME = 2;//two days
	public static final double DEFAULT_DISPERSION = 2;//2 metres/second
	public static final double THRESHOLD = 0.5;//0.5%
	public static final int DEFAULT_ALERT_THRESHOLD = 80; 
	
	public enum SupportedContagion{
		OTHER,
		COVID_19,
		SEASONAL,
		HAY_FEVER,
		CALAMITY;
		
		@Override
		public String toString() {
			return StringStyler.prettyString( super.name());
		}
		
		public static boolean isSupported( String str ) {
			if( StringUtils.isEmpty(str))
				return false;
			for( SupportedContagion sc: values() ) {
				if( sc.name().equals(str))
					return true;
			}
			return false;
		}
		
		public static String[] getItems() {
			String[] results = new String[ values().length ];
			for( int i=0; i<values().length; i++ ) {
				results[i] = values()[i].toString();
			}
			return results;
		}
	}

	public enum Attributes{
		IDENTIFIER,
		CONTAGIOUSNESS,
		TIMESTAMP;

		@Override
		public String toString() {
			return StringStyler.prettyString( super.toString());
		}
	}

	String getIdentifier();

	double getContagiousness();

	boolean isMonitored();

	void setMonitored(boolean monitored);
	
	/**
	 * If true, the infection should alert a practitioner if the contagion
	 * exceeds a certain value;
	 * @return
	 */
	boolean alert( double contagion);

	int getIncubation();

	int getDistance();

	int getHalfTime();

	double getDispersion();

	double getContagiousnessInTime(long days);

	double getContagiousnessDistance(double distance);

	/**
	 * Get the (maximum) spreading of this contagiony
	 * @param days
	 * @param distance
	 * @return
	 */
	double getSpread(long days, double distance);

	double getThreshold();
}