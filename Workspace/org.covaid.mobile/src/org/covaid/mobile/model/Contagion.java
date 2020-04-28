package org.covaid.mobile.model;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.condast.commons.data.util.Vector;
import org.condast.commons.date.DateUtils;
import org.condast.commons.number.NumberUtils;
import org.covaid.core.def.IContagion;

@Entity(name="CONTAGION")
public class Contagion implements IContagion{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable=false)
	private String identifier;

	private int maxDays;
	private int maxDistance;
	
	private int halfTime; //days
	private double dispersion;//m/s
	
	private double contagiousness;
	
	private boolean monitored;
	
	@Basic(optional = false)
	@Column( nullable=false)
	@Temporal(TemporalType.TIMESTAMP)
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

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public double getContagiousness() {
		return contagiousness;
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
	 * Updates the current contagion, based on the time and distance of the given contagion,
	 * Only update if the contagion gets worse. In that case the return value is true,
	 * otherwise false
	 * 
	 * @param contagion
	 * @param date
	 * @param distance
	 * @return
	 */
	public boolean update( IContagion contagion, Date date, double distance) {
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
}
