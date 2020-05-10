package org.covaid.core.def;

import org.condast.commons.data.util.Vector;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.strings.StringUtils;
import org.covaid.core.model.date.DateContagion;

public interface IContagion<T extends Object> extends Comparable<IContagion<T>>{

	int DAY = 24 * 3600 * 1000;//msec
	int DEFAULT_CONTAGION = 14;//two weeks
	int DEFAULT_DISTANCE = 10;//10 meters
	int DEFAULT_HALFTIME = 2;//two days
	double DEFAULT_DISPERSION = 2;//2 metres/second
	double THRESHOLD = 0.5;//0.5%

	public enum SupportedContagion{
		OTHER,
		COVID_19,
		SEASONAL,
		EXPLOSION;
		
		public DateContagion getContagion() {
			return new DateContagion(this, 100f);
		}

		@Override
		public String toString() {
			return super.toString();
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

		public static DateContagion getContagion( String str ) {
			if( !isSupported(str))
				return SupportedContagion.OTHER.getContagion();
			return SupportedContagion.valueOf(str).getContagion();
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

	int getIncubation();

	int getDistance();

	int getHalfTime();

	double getDispersion();

	T getTimestamp();

	double getContagiousnessInTime(long days);

	double getContagiousnessDistance(double distance);

	/**
	 * Get the (maximum) contagiousness of this entry
	 * @param days
	 * @param distance
	 * @return
	 */
	double getContagiousness(long days, double distance);

	/**
	 * Get the contagion as it spreads in the seconds after contact
	 * <contagion, radius>
	 * 
	 * @param contagion
	 * @param date
	 * @param distance
	 * @return
	 */
	Vector<Double, Double> getContagion(T date);

	/**
	 * Returns true if the this contagiousness is larger than the given one
	 * @param contagion
	 * @return
	 */
	boolean isLarger(IContagion<T> contagion);

	boolean isContagious(long days);

	boolean isContagious(T date);

	int compareTo(IContagion<T> o);

	double getThreshold();

}