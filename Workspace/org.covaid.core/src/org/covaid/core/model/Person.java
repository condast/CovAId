package org.covaid.core.model;

import java.util.Date;
import java.util.Map;

import org.condast.commons.date.DateUtils;
import org.condast.commons.number.NumberUtils;
import org.covaid.core.mobile.Mobile;

public class Person implements Comparable<Person>{

	public static final int DEFAULT_ILL_THRESHOLD = 75;//%
	
	public enum States{
		HEALTHY,
		FEEL_ILL,
		APPOINTMENT,
		CONFIRMATION;
	}
	private States state;
	private Contagion monitor;
	
	private Point location;
	
	private Mobile mobile;

	public Person( String identifier, int xpos, int ypos, double safety, double risk) {
		location = new Point(xpos, ypos);
		mobile = new Mobile(identifier, safety, risk, location);
		this.state = States.HEALTHY;
		this.monitor = null;
	}

	public Person( String identifier, int xpos, int ypos, double safety, double risk, Date date, Contagion contagion) {
		this( identifier, xpos, ypos, safety, risk );
		setContagion( date, contagion );
	}

	public String getIdentifier() {
		return mobile.getIdentifier();
	}

	public void setPosition(int xpos, int ypos) {
		location.setPosition(xpos, ypos);
	}

	public Point getLocation() {
		return location;
	}

	public States getState() {
		return state;
	}

	public void setState(States state) {
		this.state = state;
		switch( this.state ) {
		case FEEL_ILL:
			monitor = getHistory().getMonitor();
			monitor.setMonitored(true);
			break;
		case HEALTHY:
			if( monitor == null )
				break;
			monitor.setMonitored(false);	
			monitor = null;
		default:
			break;
		}
	}

	public Contagion getMonitor() {
		return monitor;
	}

	public Date getCurrent() {
		return this.getHistory().getCurrent();
	}
	
	public boolean isHealthy() {
		return this.mobile.isHealthy();
	}

	public void setContagion( Date date, Contagion contagion) {
		Location loc = new Location( location );
		loc.addContagion(contagion);
		this.monitor = contagion;
		if( contagion.getContagiousness() > DEFAULT_ILL_THRESHOLD) {
			state = States.FEEL_ILL;
		}
		this.mobile.alert( date, loc, contagion );
	}
	
	public void setIll( Date date ) {
		setState(States.FEEL_ILL);		
		Location loc = createSnapshot();
		loc.addContagion(monitor);
		this.mobile.getHistory().alert(date, loc, monitor);
	}
	
	public void setIll( Date date, String identifier ) {
		setState(States.FEEL_ILL);
		monitor = new Contagion( identifier, 100 );
	}
	
	public History getHistory() {
		return this.mobile.getHistory();
	}

	public void alert( Date date, Location point ) {
		this.location = point;
		this.mobile.alert( date, (Location) point, monitor);
	}

	/**
	 * Create a snapshot of the current risk of contagiousness
	 * @return
	 */
	public Location createSnapshot() {
		return getHistory().createSnapShot(getCurrent(), location);
	}

	public double getContagiousness( Contagion contagion ) {
		Location location = createSnapshot();
		return location.getContagion(contagion);
	}

	public double getContagiousness( Contagion contagion, Date date, Map.Entry<Date, Location> entry ) {
		if( entry == null )
			return 0;
		double distance = location.getDistance( entry.getValue());
		double caldist = contagion.getContagiousnessDistance(distance);
		double calctime = contagion.getContagiousnessInTime( DateUtils.getDifferenceDays( date, entry.getKey()));
		return Math.max(caldist, calctime);
	}

	/**
	 * This is the measure in which you want to protect others
	 * @return
	 */
	public double getSafetyBubble( Contagion contagion, Date date ) {
		Map.Entry<Date, Location> contagiousness = mobile.getHistory().getMaxContagiousness(contagion);
		double maxContagion = 100;
		if( contagiousness != null )
			maxContagion = getContagiousness(contagion, date, contagiousness);
		return contagion.getDistance() * mobile.getSafety()/ maxContagion;
	}

	/**
	 * This is the measure in which are willing to take a risk
	 * @return
	 */
	public double getRiskBubble( Contagion contagion ) {
		double radius = contagion.getDistance() * (100 - mobile.getRisk())/100;
		return NumberUtils.clipRange(0, 100, radius );
	}

	public void move( Point point ) {
		this.location = point;
	}

	/**
	 * Update the person in normal circumstances
	 * @param date
	 */
	public void updatePerson( Date date ) {
		History history = mobile.getHistory();
		if( !history.isEmpty() ) {
			history.clean(date);
		}
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if( super.equals(obj))
			return true;
		if(!( obj instanceof Person))
			return false;
		Person test = (Person) obj;
		return location.equals(test.getLocation());
	}

	@Override
	public int compareTo(Person o) {
		return this.location.compareTo(o.getLocation());
	}

	@Override
	public String toString() {
		return location.toString();
	}
}