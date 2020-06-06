package org.covaid.core.hub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.condast.commons.Utils;
import org.covaid.core.def.IContagion;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPersonListener;
import org.covaid.core.def.IPoint;
import org.covaid.core.hub.trace.ITrace;
import org.covaid.core.model.AbstractLocation;
import org.covaid.core.model.Point;

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
	
	private ITrace<T> trace;

	private Collection<IHubListener<T>> listeners;

	/**
	 * Response to persons who crossed this hub, who have become infectious
	 */
	private IPersonListener<T> listener = (e)->{
		ILocation<T> check = e.getSnapshot();
		if(( !check.equals( this.location)) || 
				check.isHealthy(this.timeStep) || 
				( location.isWorse(check)))
			return;
		if(!onPersonAlert(e.getPerson(), e.getMoment(), this.timeStep, history, persons.get(e.getPerson())))
			return;
		location = createSnapShot();
		location = location.createWorst(check);
		this.notifyListeners( new HubEvent<T>( this, this.trace, e.getCurrent(), e.getContagion()));
	};

	protected AbstractHub( ILocation<T> location, T initial, T history, ITrace<T> trace ) {
		super(location.getXpos(), location.getYpos());
		this.persons = new TreeMap<>();
		this.location = location;
		this.timeStep = initial;
		this.trace = trace;
		this.history = history;
		this.listeners = new ArrayList<>();
		this.previous = new TreeSet<>();
	}

	@Override
	public ILocation<T> getLocation() {
		return location;
	}

	@Override
	public boolean isHealthy( IContagion contagion) {
		return this.location.isHealthy(contagion, this.timeStep);
	}

	protected T getTimeStep() {
		return timeStep;
	}

	protected T getHistory() {
		return history;
	}

	protected ITrace<T> getTrace() {
		return trace;
	}

	@Override
	public void enableTrace( boolean enabled ) {
		this.trace.setEnabled(enabled);
	}
	
	@Override
	public boolean isTraceEnabled() {
		return this.trace.isEnabled();
	}
	
	@Override
	public Map<T, Double> getPrediction( IContagion contagion, T range ){
		return this.trace.getPrediction(contagion, range);
	}

	protected void setLocation(ILocation<T> location) {
		this.location = location;
	}

	@Override
	public double getContagion( IContagion contagion, T step ) {
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
		IContagion[] worse = this.location.getWorse( hub.getLocation() );
		ILocation<T> result = (ILocation<T>) this.location.clone();
		this.location = AbstractLocation.createWorst( result, this.location, hub.getLocation());
		boolean retval = this.previous.add( (Point) hub);
		for( IContagion contagion: worse )
			this.notifyListeners( new HubEvent<T>( this, this.trace, this.timeStep, contagion ));
		return retval;
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
	 * A snap shot is a representation of the current state of this location, with respect to
	 * the risk of contagion. In case of a Hub, the location is quite clear;
	 *  for a Person it is the current location 
	 * 
	 * @return
	 */
	@Override
	public ILocation<T> createSnapShot() {
		Iterator<Map.Entry<IPerson<T>, T>> iterator = persons.entrySet().iterator();
		ILocation<T> result = this.location.clone();
		while( iterator.hasNext()) {
			Map.Entry<IPerson<T>, T> entry = iterator.next();
			ILocation<T> snapshot = entry.getKey().createSnapshot();
			ILocation<T> test = result.createWorst(snapshot);
			if( test != null )
				result = test;
		}
		return result;
	}

	/**
	 * Respond to an encounter with a person. This happens when a person enters the location of this hub
	 * The snapshots of the person and the location are compared, and the person is alerted
	 * if the risk of infection has increased. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	@Override
	public boolean encounter( IPerson<T> person, T step ) {
		return this.encounter(person, step, null );
	}

	/**
	 * Respond to an encounter with a person. This happens when a person enters the location of this hub
	 * The snapshots of the person and the location are compared, and the person is alerted
	 * if the risk of infection has increased. Returns true if the snapshot has become worse
	 * @param person
	 * @return
	 */
	@Override
	public boolean encounter( IPerson<T> person, T step, IContagion contagion ) {
		if( !person.getLocation().equals( this.location))
			return false;
		return alert(person, contagion, step, step);
	}

	/**
	 * Respond to an alert of a person. This happens when the person has become infected, and is alerting previous locations 
	 * of the infection. Returns true if an alert was given to the person
	 * @param person
	 * @return
	 */
	protected boolean alert( IPerson<T> person, IContagion contagion, T current, T moment ) {
		if( !person.getLocation().equals( this.location ))
			return false;
		
		ILocation<T> check = person.createSnapshot();
		
		//Determine the worst case situation for the encounter
		ILocation<T> worst = this.location.createWorst( check );

		//Check if the person is worse off, and if so generate an alert		
		IContagion[] worse = check.getWorse( worst );
		boolean result = !Utils.assertNull(worse);
		if( result)
			person.alert( current, moment, worst, contagion);
		
		persons.put( person, moment );

		//now check if this location is worse off
		worse = this.location.getWorse( worst );
		if( !Utils.assertNull(worse))
			return result;
		
		this.location = worst;
		
		//Inform all the stored persons, who passed the hub later that their risk of
		//contagion has become worse
		Iterator<Map.Entry<IPerson<T>, T>> iterator = this.persons.entrySet().iterator();
		while( iterator.hasNext() ) {
			Map.Entry<IPerson<T>, T> entry = iterator.next();
			if( person.equals(entry.getKey()))
				continue;
			if( onPersonAlert(entry.getKey(), moment, timeStep, history, entry.getValue()))
				entry.getKey().alert(current, moment, this.location, contagion);
		}
		
		//Last alert other hubs of a contagion
		notifyListeners( new HubEvent<T>( this, trace, timeStep, contagion ));
		return true;
	}

	/**
	 * The conditions for alerting the persons:
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
	
	/**
	 * This trace has an increased risk of contagion. The guest should be a possible origin 
	 */
	@Override
	public void updateTrace( IContagion contagion, T current, IHub<T> guest ) {
		trace.update(contagion, current, guest.getLocation());
	}
	
	@Override
	public Map<T, Double> getTraces(IContagion contagion, T range) {
		return trace.getTraces(contagion, range);
	}
	
	
	@Override
	public Map<T, Double> getPrediction(IContagion contagion, T range, Map<IPoint, ? extends IHub<T>> hubs) {
		return this.trace.getPrediction(contagion, range, hubs);
	}

	@Override
	public ILocation<T> update( T timeStep ) {	
		this.timeStep = timeStep;
		ILocation<T> location = createSnapShot();
		//int days = (int) (2 * Location.getMaxContagionTime( super.getLocation()));
		persons.entrySet().removeIf(entry -> onRemovePersons( entry.getKey(), timeStep, this.history, entry.getValue()));
		return location;
	}

	@Override
	public String printTrace() {
		return this.trace.toString();
	}
}