package org.covaid.core.model.date;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.condast.commons.date.DateUtils;
import org.condast.commons.number.NumberUtils;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistory;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IMobile;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;
import org.covaid.core.model.Point;

public class DatePerson implements IPerson<Date>{

	private States state;
	private IContagion<Date> monitor;
	
	private IPoint location;
	
	private IMobile<Date> mobile;

	public DatePerson( Point point, double safety, double health) {
		this( point.getIdentifier(), point.getXpos(), point.getYpos(), safety, health );
	}

	public DatePerson( String identifier, int xpos, int ypos, double safety) {
		this( identifier, xpos, ypos, safety, 100 );
	}
	
	public DatePerson( String identifier, int xpos, int ypos, double safety, double health) {
		location = new Point( identifier, xpos, ypos);
		mobile = new DateMobile(identifier, safety, health, location);
		this.state = States.HEALTHY;
		this.monitor = null;
	}

	public DatePerson( String identifier, int xpos, int ypos, double safety, DateContagion contagion) {
		this( identifier, xpos, ypos, safety, 100 - contagion.getContagiousness() );
		setContagion( Calendar.getInstance().getTime(), contagion );
	}

	public DatePerson( String identifier, int xpos, int ypos, double safety, Date date, DateContagion contagion) {
		this( identifier, xpos, ypos, safety, 100 - contagion.getContagiousness() );
		setContagion( date, contagion );
	}

	@Override
	public String getIdentifier() {
		return mobile.getIdentifier();
	}

	@Override
	public void setPosition(int xpos, int ypos) {
		location.setPosition(xpos, ypos);
	}

	@Override
	public IPoint getLocation() {
		return location;
	}

	@Override
	public States getState() {
		return state;
	}

	@Override
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

	@Override
	public IContagion<Date> getMonitor() {
		return monitor;
	}

	@Override
	public Date getCurrent() {
		return this.getHistory().getCurrent();
	}
	
	@Override
	public boolean isHealthy() {
		return this.mobile.isHealthy();
	}

	@Override
	public void setContagion( Date date, IContagion<Date> contagion) {
		DateLocation loc = new DateLocation( location );
		loc.addContagion(contagion);
		this.monitor = contagion;
		if( contagion.getContagiousness() > DEFAULT_ILL_THRESHOLD) {
			state = States.FEEL_ILL;
		}
		this.mobile.alert( date, loc, contagion );
	}
	
	@Override
	public void setIll( Date date ) {
		setState(States.FEEL_ILL);		
		ILocation<Date> loc = createSnapshot();
		loc.addContagion(monitor);
		this.mobile.getHistory().alert(date, loc, monitor);
	}
	
	@Override
	public void setIll( Date date, String identifier ) {
		setState(States.FEEL_ILL);
		monitor = new DateContagion( identifier, 100 );
	}
	
	@Override
	public IHistory<Date> getHistory() {
		return this.mobile.getHistory();
	}

	@Override
	public ILocation<Date> get(Date date) {
		return this.getHistory().get(date);
	}

	@Override
	public void alert( Date date, ILocation<Date> point ) {
		this.location = point;
		this.mobile.alert( date, point, monitor);
	}

	/**
	 * Create a snapshot of the current risk of contagiousness
	 * @return
	 */
	@Override
	public ILocation<Date> createSnapshot() {
		return getHistory().createSnapShot(getCurrent(), location);
	}

	@Override
	public double getContagiousness( IContagion<Date> contagion ) {
		ILocation<Date> location = createSnapshot();
		return location.getContagion(contagion);
	}

	@Override
	public double getContagiousness( IContagion<Date> contagion, Date date, Map.Entry<Date, ILocation<Date>> entry ) {
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
	@Override
	public double getSafetyBubble( IContagion<Date> contagion, Date date ) {
		Map.Entry<Date, ILocation<Date>> contagiousness = mobile.getHistory().getMaxContagiousness(contagion);
		double maxContagion = 100;
		if( contagiousness != null )
			maxContagion = getContagiousness(contagion, date, contagiousness);
		return contagion.getDistance() * mobile.getSafety()/ maxContagion;
	}

	/**
	 * This is the measure in which are willing to take a risk
	 * @return
	 */
	@Override
	public double getRiskBubble( IContagion<Date> contagion ) {
		double radius = contagion.getDistance() * (100 - mobile.getHealth())/100;
		return NumberUtils.clipRange(0, 100, radius );
	}

	@Override
	public void move( IPoint point ) {
		this.location = point;
	}

	/**
	 * Update the person in normal circumstances
	 * @param date
	 */
	@Override
	public void updatePerson( Date date ) {
		IHistory<Date> history = mobile.getHistory();
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
		if(!( obj instanceof DatePerson))
			return false;
		@SuppressWarnings("unchecked")
		IPerson<Date> test = (IPerson<Date>) obj;
		return location.equals(test.getLocation());
	}

	@Override
	public int compareTo(IPerson<Date> o) {
		return this.location.compareTo(o.getLocation());
	}

	@Override
	public String toString() {
		return location.toString();
	}
}