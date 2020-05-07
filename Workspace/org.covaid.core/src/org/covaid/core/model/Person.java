package org.covaid.core.model;

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

public class Person implements IPerson{

	private States state;
	private IContagion monitor;
	
	private IPoint location;
	
	private IMobile mobile;

	public Person( Point point, double safety, double risk) {
		this( point.getIdentifier(), point.getXpos(), point.getYpos(), safety, risk );
	}
	
	public Person( String identifier, int xpos, int ypos, double safety, double risk) {
		location = new Point( identifier, xpos, ypos);
		mobile = new Mobile(identifier, safety, risk, location);
		this.state = States.HEALTHY;
		this.monitor = null;
	}

	public Person( String identifier, int xpos, int ypos, double safety, double risk, Date date, Contagion contagion) {
		this( identifier, xpos, ypos, safety, risk );
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
	public IContagion getMonitor() {
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
	public void setContagion( Date date, IContagion contagion) {
		Location loc = new Location( location );
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
		ILocation loc = createSnapshot();
		loc.addContagion(monitor);
		this.mobile.getHistory().alert(date, loc, monitor);
	}
	
	@Override
	public void setIll( Date date, String identifier ) {
		setState(States.FEEL_ILL);
		monitor = new Contagion( identifier, 100 );
	}
	
	@Override
	public IHistory getHistory() {
		return this.mobile.getHistory();
	}

	@Override
	public ILocation get(Date date) {
		return this.getHistory().get(date);
	}

	@Override
	public void alert( Date date, ILocation point ) {
		this.location = point;
		this.mobile.alert( date, point, monitor);
	}

	/**
	 * Create a snapshot of the current risk of contagiousness
	 * @return
	 */
	@Override
	public ILocation createSnapshot() {
		return getHistory().createSnapShot(getCurrent(), location);
	}

	@Override
	public double getContagiousness( IContagion contagion ) {
		ILocation location = createSnapshot();
		return location.getContagion(contagion);
	}

	@Override
	public double getContagiousness( IContagion contagion, Date date, Map.Entry<Date, ILocation> entry ) {
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
	public double getSafetyBubble( IContagion contagion, Date date ) {
		Map.Entry<Date, ILocation> contagiousness = mobile.getHistory().getMaxContagiousness(contagion);
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
	public double getRiskBubble( IContagion contagion ) {
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
		IHistory history = mobile.getHistory();
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
		IPerson test = (IPerson) obj;
		return location.equals(test.getLocation());
	}

	@Override
	public int compareTo(IPerson o) {
		return this.location.compareTo(o.getLocation());
	}

	@Override
	public String toString() {
		return location.toString();
	}
}