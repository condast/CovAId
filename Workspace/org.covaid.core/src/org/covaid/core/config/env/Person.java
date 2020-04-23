package org.covaid.core.config.env;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.condast.commons.date.DateUtils;
import org.covaid.core.mobile.Mobile;

public class Person implements Comparable<Person>{

	private Point location;
	
	private Mobile mobile;
	
	public Person( int xpos, int ypos, double safety, double risk) {
		location = new Location(xpos, ypos);
		mobile = new Mobile(location.toString(), safety, risk, location);
	}

	public Person( int xpos, int ypos, double safety, double risk, Contagion contagion) {
		this( xpos, ypos, safety, risk );
		Location loc = new Location( location );
		loc.addContagion(contagion);
		this.putHistory( Calendar.getInstance().getTime(), loc);
	}

	public void setPosition(int xpos, int ypos) {
		location.setPosition(xpos, ypos);
	}

	public Point getLocation() {
		return location;
	}

	public void putHistory( Date date, Location location ) {
		this.mobile.putHistory( date, location);
	}

	public History getHistory() {
		return this.mobile.getHistory();
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
		if( contagiousness == null )
			return mobile.getSafety();
		double maxContagion = getContagiousness(contagion, date, contagiousness);
		double radius = maxContagion * contagion.getDistance() * mobile.getSafety();
		return radius;
	}

	/**
	 * This is the measure in which are willing to take a risk
	 * @return
	 */
	public double getRiskBubble( Contagion contagion ) {
		double radius = contagion.getDistance() * mobile.getRisk();
		return radius;
	}
	
	/**
	 * Update the person in normal circumstances
	 * @param date
	 * @param x
	 * @param y
	 */
	public void updatePerson( Date date, int x, int y ) {
		History history = mobile.getHistory();
		Location location = new Location(x, y);
		if( !history.isEmpty() ) {
			history.clean(date);
			if( history.getRecent() != null ) {
				for( Contagion contagion: history.getRecent().getContagion()) {
					long days = DateUtils.getDifferenceDays(date, history.getCurrent());
					double adjusted = contagion.getContagiousnessInTime(days);
					if( adjusted >= Contagion.THRESHOLD ) 
						location.addContagion( new Contagion( contagion.getIdentifier(), adjusted ));
				}
			}
		}
		history.put(date, location);
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