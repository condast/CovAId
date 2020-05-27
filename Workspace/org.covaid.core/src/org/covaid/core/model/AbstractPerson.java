package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.condast.commons.number.NumberUtils;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.IHistory;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IMobile;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPersonListener;
import org.covaid.core.def.IPoint;
import org.covaid.core.def.PersonEvent;

public abstract class AbstractPerson<T extends Object> implements IPerson<T>{

	private States state;
	private IContagion monitor;
	
	private IPoint point;
	
	private IMobile<T> mobile;
	
	private Collection<IPersonListener<T>> listeners;
	
	protected AbstractPerson( int xpos, int ypos, IContagion contagion, IMobile<T> mobile) {
		point = new Point( mobile.getIdentifier(), xpos, ypos);
		this.mobile = mobile;
		this.state = States.HEALTHY;
		this.monitor = contagion;
		listeners = new ArrayList<>();
	}

	@Override
	public String getIdentifier() {
		return mobile.getIdentifier();
	}

	@Override
	public void setPosition(int xpos, int ypos) {
		point = new Point( mobile.getIdentifier(), xpos, ypos);
	}

	@Override
	public IPoint getLocation() {
		return point;
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
	public T getCurrent() {
		return this.getHistory().getCurrent();
	}
	
	@Override
	public boolean isHealthy() {
		return this.mobile.isHealthy();
	}

	@Override
	public void addListener( IPersonListener<T> listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener( IPersonListener<T> listener) {
		this.listeners.remove(listener);
	}
	
	protected void notifyListeners( PersonEvent<T> event) {
		for( IPersonListener<T> listener: this.listeners )
			listener.notifyPersonChanged(event);
	}

	protected abstract long getDifference( T first, T last );

	protected abstract IContagion createContagion( String identifier, double safety );

	protected abstract ILocation<T> createLocation( String identifier, IPoint point );

	@Override
	public void setContagion( T current, T moment, IContagion contagion) {
		ILocation<T> loc = createLocation( point.getIdentifier(),  point );
		loc.addContagion( moment, contagion);
		this.monitor = contagion;
		if( contagion.getContagiousness() > DEFAULT_ILL_THRESHOLD) {
			state = States.FEEL_ILL;
		}
		this.alert( current, moment, loc, contagion );
	}
	
	@Override
	public void setIll( T step ) {
		setState(States.FEEL_ILL);		
		ILocation<T> loc = createSnapshot();
		loc.addContagion( step, monitor);
		this.mobile.getHistory().alert(step, loc, monitor, 100);
	}
	
	@Override
	public void setIll( T date, String identifier ) {
		setState(States.FEEL_ILL);
		monitor = this.createContagion( identifier, 100 );
	}
	
	protected IHistory<T> getHistory() {
		return this.mobile.getHistory();
	}

	@Override
	public ILocation<T> get(T date) {
		return this.getHistory().get(date);
	}

	@Override
	public void alert( T current, T moment, IPoint location, IContagion contagion) {
		this.mobile.alert( moment, location, monitor);
		this.notifyListeners( new PersonEvent<T>( this, current, moment, contagion, createSnapshot()));
	}

	/**
	 * Create a snapshot of the current risk of contagiousness
	 * @return
	 */
	@Override
	public ILocation<T> createSnapshot() {
		return getHistory().createSnapShot(point);
	}

	@Override
	public double getContagiousness( IContagion contagion, T step ) {
		ILocation<T> location = createSnapshot();
		return location.getContagion(contagion, step);
	}

	@Override
	public double getContagiousness( IContagion contagion, T step, Map.Entry<T, ILocation<T>> entry ) {
		if( entry == null )
			return 0;
		double distance = point.getDistance( entry.getValue());
		double caldist = contagion.getContagiousnessDistance(distance);
		double calctime = contagion.getContagiousnessInTime( getDifference( step, entry.getKey()));
		return Math.max(caldist, calctime);
	}

	/**
	 * This is the measure in which you want to protect others
	 * @return
	 */
	@Override
	public double getSafetyBubble( IContagion contagion, T step ) {
		Map.Entry<T, ILocation<T>> contagiousness = mobile.getHistory().getMaxContagiousness(contagion);
		double maxContagion = 100;
		if( contagiousness != null )
			maxContagion = getContagiousness(contagion, step, contagiousness);
		return contagion.getDistance() * mobile.getRisk()/ maxContagion;
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
		this.point = point;
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
		return point.equals(test.getLocation());
	}

	@Override
	public int compareTo(IPerson<T> o) {
		return this.point.compareTo(o.getLocation());
	}

	@Override
	public String toString() {
		return point.toString();
	}
}