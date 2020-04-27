package org.covaid.core.model;

import java.util.Calendar;
import java.util.Date;
import org.condast.commons.data.util.Vector;
import org.condast.commons.date.DateUtils;
import org.condast.commons.number.NumberUtils;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.strings.StringUtils;

public class Contagion implements Comparable<Contagion>{

	public static final int DAY = 24*3600*1000;//msec

	public static final int DEFAULT_CONTAGION = 14;//two weeks
	public static final int DEFAULT_DISTANCE = 10;//10 meters
	public static final int DEFAULT_HALFTIME = 2;//two days
	public static final double DEFAULT_DISPERSION = 2;//2 metres/second

	public static final double THRESHOLD = 0.5;//0.5%

	public enum SupportedContagion{
		OTHER,
		COVID_19,
		SEASONAL,
		EXPLOSION;
		
		public Contagion getContagion() {
			return new Contagion(this, 100f);
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

		public static Contagion getContagion( String str ) {
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

	private String identifier;
	private int maxDays;
	private int maxDistance;
	
	private int halfTime; //days
	private double dispersion;//m/s
	
	private double contagiousness;
	
	private boolean monitored;
	
	private Date timestamp;
	
	public Contagion( String identifier, double contagiousness) {
		this( identifier, contagiousness, DEFAULT_DISTANCE, DEFAULT_CONTAGION );
	}

	public Contagion( SupportedContagion identifier, double contagiousness) {
		this( identifier.toString(), contagiousness, DEFAULT_DISTANCE, DEFAULT_CONTAGION );
	}

	public Contagion(String identifier, double contagiousness, int distance, int maxDays ) {
		this( identifier, contagiousness, distance, maxDays, DEFAULT_HALFTIME, DEFAULT_DISPERSION, false );
	}
	
	public Contagion(String identifier, double contagiousness, int distance, int maxDays, int halftime, double dispersion, boolean monitored ) {
		super();
		this.identifier = identifier;
		this.contagiousness = contagiousness;
		this.monitored = monitored;
		this.maxDistance = distance;
		this.maxDays = maxDays;
		this.halfTime= halftime;
		this.dispersion = dispersion;
		this.timestamp = Calendar.getInstance().getTime();
	}

	public String getIdentifier() {
		return identifier;
	}

	public double getContagiousness() {
		return contagiousness;
	}
	
	public boolean isMonitored() {
		return monitored;
	}

	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
	}

	public int getIncubation() {
		return maxDays;
	}

	public int getDistance() {
		return maxDistance;
	}

	public int getHalfTime() {
		return halfTime;
	}

	public double getDispersion() {
		return dispersion;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public double getContagiousnessInTime( long days ) {
		if( days == 0 )
			return this.contagiousness;
		int half = (int)this.maxDays/2;
		long day = (days <= half)? days: (days >= maxDays)?0: (maxDays - days);
		double calculated = NumberUtils.clipRange(0, 100, this.contagiousness/day);
		
		if( !SupportedContagion.isSupported(identifier)) {
			return calculated;
		}
		switch( SupportedContagion.valueOf(identifier)) {
		case COVID_19:
			break;
		default:
			break;
		}
		return calculated;
	}

	public double getContagiousnessDistance( double distance ) {
		int half = (int)this.maxDistance/2;
		double calculated = (distance <= half)? distance: (distance >= maxDistance)?0: (maxDistance-distance);
		if( !SupportedContagion.isSupported(identifier)) {
			return calculated;
		}
		switch( SupportedContagion.valueOf(identifier)) {
		case COVID_19:
			break;
		default:
			break;
		}
		return calculated;
	}

	/**
	 * Get the (maximum) contagiousness of this entry
	 * @param days
	 * @param distance
	 * @return
	 */
	public double getContagiousness( long days, double distance ) {
		double time = getContagiousnessInTime(days);
		double dist = getContagiousnessDistance(distance);
		return Math.max(time, dist);
	}

	/**
	 * Get the contagion as it spreads in the seconds after contact
	 * <contagion, radius>
	 * 
	 * @param contagion
	 * @param date
	 * @param distance
	 * @return
	 */
	public Vector<Double,Double> getContagion( Date date) {
		long diff = Math.abs( this.timestamp.getTime()- date.getTime());
		double newContagion = NumberUtils.clipRange(0, 100, this.contagiousness * this.halfTime * DAY/diff);
		double radius = this.maxDistance + this.dispersion* diff*1000;
		return new Vector<Double, Double>(newContagion, radius );	
	}

	/**
	 * Updates the current contagion, based on the time and distance of the given contagion,
	 * Only update if the contagion gets worse. In that case the return value is true,
	 * otherwise false
	 * 
	 * @param contagion
	 * @param date
	 * @param distance
	 * @return
	 */
	public boolean update( Contagion contagion, Date date, double distance) {
		if( !this.identifier.equals(contagion.getIdentifier()))
			return false;
		double newContagionOnDistance = contagion.getContagiousnessDistance( distance);
		long days = DateUtils.getDifferenceDays(this.timestamp, date);
		double newContagionOnTime = contagion.getContagiousnessInTime( days );
		double newContagion = Math.max(newContagionOnDistance, newContagionOnTime);
		if( newContagion <= this.contagiousness)
			return false;
		this.contagiousness = (double) newContagion;
		return true;	
	}
	
	/**
	 * Returns true if the this contagiousness is larger than the given one
	 * @param contagion
	 * @return
	 */
	public boolean isLarger( Contagion contagion ) {
		return contagion.getContagiousness() < this.contagiousness;
	}

	public boolean isContagious( long days ) {
		return days < this.maxDays;
	}

	public boolean isContagious( Date date) {
		return this.isContagious( DateUtils.getDifferenceDays( date, getTimestamp()));
	}

	public void update( Date date ) {
		this.timestamp = date;		
	}

	
	@Override
	public int hashCode() {
		return this.identifier.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if( super.equals(obj))
			return true;
		if(!( obj instanceof Contagion ))
			return false;
		Contagion test = (Contagion) obj;
		return this.identifier.equals(test.getIdentifier());
	}

	@Override
	public int compareTo(Contagion o) {
		return this.identifier.compareTo(o.getIdentifier());
	}
}
