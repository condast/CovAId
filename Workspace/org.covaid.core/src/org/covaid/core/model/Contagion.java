package org.covaid.core.model;

import java.util.Calendar;
import java.util.Date;

import org.condast.commons.data.util.Vector;
import org.condast.commons.date.DateUtils;
import org.condast.commons.number.NumberUtils;
import org.covaid.core.def.IContagion;

public class Contagion implements IContagion{

	private String identifier;

	private int maxDays;
	private int maxDistance;
	
	private int halfTime; //days
	private double dispersion;//m/s
	
	private double contagiousness;
	
	private boolean monitored;
	
	private double threshold;//percent
	
	private Date timestamp;
	
	public Contagion( String identifier, double contagiousness) {
		this( identifier, contagiousness, DEFAULT_DISTANCE, DEFAULT_CONTAGION );
	}

	public Contagion( SupportedContagion identifier, double contagiousness) {
		this( identifier.toString(), contagiousness, DEFAULT_DISTANCE, DEFAULT_CONTAGION );
	}

	public Contagion(String identifier, double contagiousness, int distance, int maxDays ) {
		this( identifier, contagiousness, THRESHOLD, distance, maxDays, DEFAULT_HALFTIME, DEFAULT_DISPERSION, false );
	}
	
	public Contagion(String identifier, double contagiousness, double threshold, int distance, int maxDays, int halftime, double dispersion, boolean monitored ) {
		super();
		this.identifier = identifier;
		this.contagiousness = contagiousness;
		this.monitored = monitored;
		this.threshold = threshold;
		this.maxDistance = distance;
		this.maxDays = maxDays;
		this.halfTime= halftime;
		this.dispersion = dispersion;
		this.timestamp = Calendar.getInstance().getTime();
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public double getContagiousness() {
		return contagiousness;
	}
	
	@Override
	public double getThreshold() {
		return threshold;
	}

	@Override
	public boolean isMonitored() {
		return monitored;
	}

	@Override
	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
	}

	@Override
	public int getIncubation() {
		return maxDays;
	}

	@Override
	public int getDistance() {
		return maxDistance;
	}

	@Override
	public int getHalfTime() {
		return halfTime;
	}

	@Override
	public double getDispersion() {
		return dispersion;
	}

	@Override
	public Date getTimestamp() {
		return timestamp;
	}

	@Override
	public double getContagiousnessInTime( long days ) {
		int half = (int)this.maxDays/2;
		long day = (days <= half)? 1: days - half;
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

	@Override
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
	@Override
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
	@Override
	public Vector<Double,Double> getContagion( Date date) {
		long diff = Math.abs( this.timestamp.getTime()- date.getTime());
		double newContagion = NumberUtils.clipRange(0, 100, this.contagiousness * this.halfTime * DAY/diff);
		double radius = this.maxDistance + this.dispersion* diff*1000;
		return new Vector<Double, Double>(newContagion, radius );	
	}
	
	/**
	 * Returns true if the this contagiousness is larger than the given one
	 * @param contagion
	 * @return
	 */
	@Override
	public boolean isLarger( IContagion contagion ) {
		return contagion.getContagiousness() < this.contagiousness;
	}

	@Override
	public boolean isContagious( long days ) {
		return days < this.maxDays;
	}

	@Override
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
		IContagion test = (IContagion) obj;
		return this.identifier.equals(test.getIdentifier());
	}

	@Override
	public int compareTo( IContagion o) {
		return this.identifier.compareTo(o.getIdentifier());
	}

	@Override
	public String toString() {
		return this.identifier + ": " + this.contagiousness;
	}
}
