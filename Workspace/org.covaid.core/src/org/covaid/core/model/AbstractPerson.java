package org.covaid.core.model;

import java.util.Map;

import org.condast.commons.number.NumberUtils;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistory;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IMobile;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPoint;

public abstract class AbstractPerson<T extends Object> implements IPerson<T>{

	private States state;
	private IContagion<T> monitor;
	
	private IPoint location;
	
	private IMobile<T> mobile;
	
	protected AbstractPerson( int xpos, int ypos, IContagion<T> contagion, IMobile<T> mobile) {
		location = new Point( mobile.getIdentifier(), xpos, ypos);
		this.mobile = mobile;
		this.state = States.HEALTHY;
		this.monitor = contagion;
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
	public IContagion<T> getMonitor() {
		return monitor;
	}

	@Override
	public T getCurrent() {
		return this.getHistory().getCurrent();
	}
	
	@Override
	public boolean isHealthy() {
		return this.mobile.isHealthy();
	}

	protected abstract long getDifference( T first, T last );

	protected abstract IContagion<T> createContagion( String identifier, double safety );

	protected abstract ILocation<T> createLocation( String identifier, IPoint point );

	@Override
	public void setContagion( T date, IContagion<T> contagion) {
		ILocation<T> loc = createLocation( location.getIdentifier(),  location );
		loc.addContagion(contagion);
		this.monitor = contagion;
		if( contagion.getContagiousness() > DEFAULT_ILL_THRESHOLD) {
			state = States.FEEL_ILL;
		}
		this.mobile.alert( date, loc, contagion );
	}
	
	@Override
	public void setIll( T date ) {
		setState(States.FEEL_ILL);		
		ILocation<T> loc = createSnapshot();
		loc.addContagion(monitor);
		this.mobile.getHistory().alert(date, loc, monitor);
	}
	
	@Override
	public void setIll( T date, String identifier ) {
		setState(States.FEEL_ILL);
		monitor = this.createContagion( identifier, 100 );
	}
	
	@Override
	public IHistory<T> getHistory() {
		return this.mobile.getHistory();
	}

	@Override
	public ILocation<T> get(T date) {
		return this.getHistory().get(date);
	}

	@Override
	public void alert( T date, ILocation<T> point ) {
		this.location = point;
		this.mobile.alert( date, point, monitor);
	}

	/**
	 * Create a snapshot of the current risk of contagiousness
	 * @return
	 */
	@Override
	public ILocation<T> createSnapshot() {
		return getHistory().createSnapShot(getCurrent(), location);
	}

	@Override
	public double getContagiousness( IContagion<T> contagion ) {
		ILocation<T> location = createSnapshot();
		return location.getContagion(contagion);
	}

	@Override
	public double getContagiousness( IContagion<T> contagion, T step, Map.Entry<T, ILocation<T>> entry ) {
		if( entry == null )
			return 0;
		double distance = location.getDistance( entry.getValue());
		double caldist = contagion.getContagiousnessDistance(distance);
		double calctime = contagion.getContagiousnessInTime( getDifference( step, entry.getKey()));
		return Math.max(caldist, calctime);
	}

	/**
	 * This is the measure in which you want to protect others
	 * @return
	 */
	@Override
	public double getSafetyBubble( IContagion<T> contagion, T step ) {
		Map.Entry<T, ILocation<T>> contagiousness = mobile.getHistory().getMaxContagiousness(contagion);
		double maxContagion = 100;
		if( contagiousness != null )
			maxContagion = getContagiousness(contagion, step, contagiousness);
		return contagion.getDistance() * mobile.getSafety()/ maxContagion;
	}

	/**
	 * This is the measure in which are willing to take a risk
	 * @return
	 */
	@Override
	public double getRiskBubble( IContagion<T> contagion ) {
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
	public void updatePerson( T date ) {
		IHistory<T> history = mobile.getHistory();
		if( !history.isEmpty() ) {
			history.clean(date);
		}
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if( super.equals(obj))
			return true;
		if(!( obj instanceof AbstractPerson))
			return false;
		IPerson<T> test = (IPerson<T>) obj;
		return location.equals(test.getLocation());
	}

	@Override
	public int compareTo(IPerson<T> o) {
		return this.location.compareTo(o.getLocation());
	}

	@Override
	public String toString() {
		return location.toString();
	}
}