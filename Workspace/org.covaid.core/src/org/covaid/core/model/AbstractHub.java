package org.covaid.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPersonListener;
import org.covaid.core.def.IPoint;
import org.covaid.core.hub.HubEvent;
import org.covaid.core.hub.IHub;
import org.covaid.core.hub.IHubListener;

public abstract class AbstractHub<T extends Object> extends Point implements IHub<T>{

	
	//A list of person identifiers and when they were present here
	private Map<IPerson<T>,T> persons;
	
	private ILocation<T> location;
	
	//The current time
	private T timeStep;
	
	//The history is the maximum 'age' of the persons that are maintained) 
	private T history;

	//The previous hubs, that usually imply that a person had moved to this hub through the other
	private Collection<Point> previous;

	private Collection<IHubListener<T>> listeners;

	private IPersonListener<T> listener = (e)->{
		ILocation<T> check = e.getSnapshot();
		if(( !check.equals( this.location)) || 
				check.isHealthy() || 
				( location.isWorse(check)))
			return;
		if(!onPersonAlert(e.getPerson(), e.getMoment(), this.timeStep, history, persons.get(e.getPerson())))
			return;
		location = createSnapShot();
		location = location.createWorst(check);
	};

	protected AbstractHub( ILocation<T> location, T initial, T history ) {
		super(location.getXpos(), location.getYpos());
		this.persons = new TreeMap<>();
		this.location = location;
		this.timeStep = initial;
		this.history = history;
		this.listeners = new ArrayList<>();
		this.previous = new TreeSet<>();
	}

	@Override
	public ILocation<T> getLocation() {
		return location;
	}

	@Override
	public boolean isHealthy( IContagion<T> contagion) {
		return this.location.isHealthy(contagion);
	}

	protected T getTimeStep() {
		return timeStep;
	}

	protected T getHistory() {
		return history;
	}

	protected void setLocation(ILocation<T> location) {
		this.location = location;
	}

	@Override
	public double getContagion( IContagion<T> contagion, T step ) {
		return this.location.getContagion(contagion, step);
	}
	
	public boolean isEmpty() {
		return this.persons.isEmpty();
	}
	
	protected void put(IPerson<T> person, T step ) {
		this.persons.put(person, step );
		person.addListener(listener);
	}

	@Override
	public Map<IPerson<T>,T> getPersons() {
		return persons;
	}

	@Override
	public void addListener( IHubListener<T> listener ) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener( IHubListener<T> listener ) {
		this.listeners.remove(listener);
	}

	protected void notifyListeners( HubEvent<T> event ) {
		for( IHubListener<T> listener: this.listeners )
			listener.notifyHubChanged(event);
	}

	@Override
	public boolean addPrevious( IHub<T> hub ) {
		if( hub == null )
			return false;
		ILocation<T> result = (ILocation<T>) this.location.clone();
		this.location = AbstractLocation.createWorst( result, this.location, hub.getLocation());
		return this.previous.add( (Point) hub);
	}

	@Override
	public boolean removePrevious( IPoint point ) {
		return this.previous.remove( point );
	}

	@Override
	public IPoint[] getPrevious() {
		return previous.toArray( new IPoint[ previous.size()]);
	}

	/**
	 * The conditions for removing a person:
	 * @param person: the person to be tested for removal
	 * @param moment is the moment of the alert, for instance hen an infection has been added in retrospect
	 * @param timeStep is the current time
	 * @param history: the time value given for removal
	 * @param encountered: the moment when the person was added to the hub
	 * @return true if action is required
	 */
	protected abstract boolean onPersonAlert( IPerson<T> person, T moment, T timeStep, T history, T encountered );

	/**
	 * The conditions for removing a person:
	 * @param person: the person to be tested for removal
	 * @param timeStep is the current time
	 * @param history: the time value given for removal
	 * @param encountered: the moment when the person was added to the hub
	 * @return
	 */
	protected abstract boolean onRemovePersons( IPerson<T> person, T timeStep, T history, T encountered );
	
	@Override
	public ILocation<T> update( T timeStep ) {	
		this.timeStep = timeStep;
		ILocation<T> location = createSnapShot();
		//int days = (int) (2 * Location.getMaxContagionTime( super.getLocation()));
		persons.entrySet().removeIf(entry -> onRemovePersons( entry.getKey(), timeStep, this.history, entry.getValue()));
		return location;
	}

}