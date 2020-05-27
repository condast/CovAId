package org.covaid.core.model;

import org.condast.commons.number.NumberUtils;
import org.condast.commons.strings.StringUtils;
import org.covaid.core.def.IContagion;

public class Contagion implements IContagion{

	public static final String S_ERR_INVALID_IDENTIFIER = "The identifier may not be NULL";
	
	private String identifier;

	private int incubation;
	private int maxDistance;
	
	private int halfTime; //days
	private double dispersion;//m/s
	
	private double contagiousness;
	
	private boolean monitored;
	
	private double threshold;//percent
	
	public Contagion( String identifier, double contagiousness) {
		this( identifier, contagiousness, DEFAULT_DISTANCE, DEFAULT_CONTAGION );
	}

	public Contagion( SupportedContagion identifier, double contagiousness) {
		this( identifier.toString(), contagiousness, DEFAULT_DISTANCE, DEFAULT_CONTAGION );
	}

	public Contagion(SupportedContagion supported) {
		this( supported, 100 );
	}

	protected Contagion(String identifier, double contagiousness, int distance, int maxDays ) {
		this( identifier, contagiousness, THRESHOLD, distance, maxDays, DEFAULT_HALFTIME, DEFAULT_DISPERSION, false );
	}
	
	protected Contagion(String identifier, double contagiousness, double threshold, int distance, int incubation, int halftime, double dispersion, boolean monitored ) {
		super();
		this.identifier = identifier;
		if( StringUtils.isEmpty(identifier))
			throw new IllegalArgumentException( S_ERR_INVALID_IDENTIFIER);
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

	/**
	 * If true, the infection should alert a practitioner if the contagion
	 * exceeds a certain value;
	 * @return
	 */
	public boolean alert( double contagion) {
		return contagion > DEFAULT_ALERT_THRESHOLD;
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
