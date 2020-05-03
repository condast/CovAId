package org.covaid.mobile.model;

import java.util.Date;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.condast.commons.date.DateUtils;
import org.condast.commons.number.NumberUtils;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistory;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;

@Entity(name="PERSON")
public class Person implements IPerson{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@OneToOne
	private Point location;
	
	private Contagion monitor;
	
	private Mobile mobile;
	
	private String state;

	public Person( String identifier, int xpos, int ypos, double safety, double risk) {
		location = new Point(xpos, ypos);
		mobile = new Mobile(identifier, safety, risk, location);
		this.state = States.HEALTHY.name();
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

	public IPoint getLocation() {
		return location;
	}

	public States getState() {
		return States.valueOf( state );
	}

	public void setState(States state) {
		this.state = state.name();
		switch( state ) {
		case FEEL_ILL:
			monitor = (Contagion) getHistory().getMonitor();
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

	public void setContagion( Date date, IContagion contagion) {
		Location loc = new Location( location );
		loc.addContagion(contagion);
		this.monitor = (Contagion) contagion;
		if( contagion.getContagiousness() > DEFAULT_ILL_THRESHOLD) {
			state = States.FEEL_ILL.name();
		}
		this.mobile.alert( date, loc, contagion );
	}
	
	public void setIll( Date date ) {
		setState(States.FEEL_ILL);		
		ILocation loc = createSnapshot();
		loc.addContagion(monitor);
		this.mobile.getHistory().alert(date, loc, monitor);
	}
	
	public void setIll( Date date, String identifier ) {
		setState(States.FEEL_ILL);
		monitor = new Contagion( identifier, 100 );
	}
	
	public IHistory getHistory() {
		return this.mobile.getHistory();
	}

	public void alert( Date date, ILocation point ) {
		this.location = (Point) point;
		this.mobile.alert( date, point, monitor);
	}

	/**
	 * Create a snapshot of the current risk of contagiousness
	 * @return
	 */
	public ILocation createSnapshot() {
		return getHistory().createSnapShot(getCurrent(), location);
	}

	public double getContagiousness( IContagion contagion ) {
		ILocation location = createSnapshot();
		return location.getContagion(contagion);
	}

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
	public double getRiskBubble( IContagion contagion ) {
		double radius = contagion.getDistance() * (100 - mobile.getRisk())/100;
		return NumberUtils.clipRange(0, 100, radius );
	}

	@Override
	public ILocation get( Date date ) {
		return this.getHistory().get(date);
	}
	
	@Override
	public void move( IPoint point ) {
		this.location = (Point) point;
	}

	/**
	 * Update the person in normal circumstances
	 * @param date
	 */
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
		Person test = (Person) obj;
		return location.equals(test.getLocation());
	}

	@Override
	public int compareTo( IPerson o) {
		return this.location.compareTo(o.getLocation());
	}

	@Override
	public String toString() {
		return location.toString();
	}
}