package org.covaid.core.config.env;

import java.util.Calendar;
import java.util.Date;

import org.condast.commons.date.DateUtils;
import org.condast.commons.strings.StringStyler;
import org.condast.commons.strings.StringUtils;

public class Contagion implements Comparable<Contagion>{

	public static final int DEFAULT_CONTAGION = 14;//two weeks
	public static final int DEFAULT_DISTANCE = 10;//10 meters

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
			return StringStyler.prettyString( super.toString());
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
	
	private float contagiousness;
	
	private Date timestamp;
	
	public Contagion( String identifier, float contagiousness) {
		this( identifier, contagiousness, DEFAULT_DISTANCE, DEFAULT_CONTAGION );
	}

	public Contagion( SupportedContagion identifier, float contagiousness) {
		this( identifier.toString(), contagiousness, DEFAULT_DISTANCE, DEFAULT_CONTAGION );
	}

	public Contagion(String identifier, float contagiousness, int distance, int maxDays) {
		super();
		this.identifier = identifier;
		this.contagiousness = contagiousness;
		this.maxDistance = distance;
		this.maxDays = maxDays;
		this.timestamp = Calendar.getInstance().getTime();
	}

	public String getIdentifier() {
		return identifier;
	}

	public float getContagiousness() {
		return contagiousness;
	}
	
	public int getMaxDays() {
		return maxDays;
	}

	public int getDistance() {
		return maxDistance;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public float getContagiousnessInTime( long days ) {
		int half = (int)this.maxDays/2;
		long day = (days <= half)? days: (days >= maxDays)?0: (maxDays - days);
		float calculated = this.contagiousness/day; 
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
	 * Updates the current contagion, based on the time and distance of the given contagion,
	 * Only update if the contagion is the same and gets worse. In that case the return value is true,
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
		this.contagiousness = (float) newContagion;
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
	public int compareTo(Contagion o) {
		return this.identifier.compareTo(o.getIdentifier());
	}
}
