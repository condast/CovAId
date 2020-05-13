package org.covaid.core.model;

import org.condast.commons.data.util.Vector;
import org.condast.commons.number.NumberUtils;
import org.covaid.core.def.IContagion;

public abstract class AbstractContagion<T extends Object> implements IContagion<T>{

	private String identifier;

	private int incubation;
	private int maxDistance;
	
	private int halfTime; //days
	private double dispersion;//m/s
	
	private double contagiousness;
	
	private boolean monitored;
	
	private double threshold;//percent
	
	protected AbstractContagion( String identifier, double contagiousness) {
		this( identifier, contagiousness, DEFAULT_DISTANCE, DEFAULT_CONTAGION );
	}

	protected AbstractContagion( SupportedContagion identifier, double contagiousness) {
		this( identifier.toString(), contagiousness, DEFAULT_DISTANCE, DEFAULT_CONTAGION );
	}

	protected AbstractContagion(String identifier, double contagiousness, int distance, int maxDays ) {
		this( identifier, contagiousness, THRESHOLD, distance, maxDays, DEFAULT_HALFTIME, DEFAULT_DISPERSION, false );
	}
	
	protected AbstractContagion(String identifier, double contagiousness, double threshold, int distance, int incubation, int halftime, double dispersion, boolean monitored ) {
		super();
		this.identifier = identifier;
		this.contagiousness = contagiousness;
		this.monitored = monitored;
		this.threshold = threshold;
		this.maxDistance = distance;
		this.incubation = incubation;
		this.halfTime= halftime;
		this.dispersion = dispersion;
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
		return incubation;
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
	public double getContagiousnessInTime( long days ) {
		int half = (int)this.incubation/2;
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
	public double getSpread( long days, double distance ) {
		double time = getContagiousnessInTime(days);
		double dist = getContagiousnessDistance(distance);
		return Math.max(time, dist);
	}

	protected abstract long getDifference( T first, T last );

	/**
	 * Get the contagion as it spreads in the seconds after contact
	 * <contagion, radius>
	 * 
	 * @param contagion
	 * @param step
	 * @param distance
	 * @return
	 */
	@Override
	public Vector<Double,Double> getContagion( T init, T step) {
		long diff = Math.abs( getDifference( init, step ));
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
	public boolean isLarger( IContagion<T> contagion ) {
		return contagion.getContagiousness() < this.contagiousness;
	}

	@Override
	public boolean isContagious(T step) {
		double contagiousness = getContagiousness();
		return ( contagiousness > this.threshold );
	}

	@Override
	public double getContagiousness( T init, T step) {
		long diff = getDifference( step, init);
		return 100*(( diff < this.incubation )? 1: (double)this.incubation/diff);
	}

	@Override
	public int hashCode() {
		return this.identifier.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if( super.equals(obj))
			return true;
		if(!( obj instanceof AbstractContagion ))
			return false;
		IContagion<?> test = (IContagion<?>) obj;
		return this.identifier.equals(test.getIdentifier());
	}

	@Override
	public int compareTo( IContagion<T> o) {
		return this.identifier.compareTo(o.getIdentifier());
	}

	@Override
	public String toString() {
		return this.identifier + ": " + this.contagiousness;
	}
}
