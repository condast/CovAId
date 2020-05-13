package org.covaid.core.model;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.covaid.core.def.IHub;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;
import org.covaid.core.def.IPersonListener;
import org.covaid.core.def.IPoint;

public abstract class AbstractHub<T extends Object> extends Point implements IHub<T>{

	
	//A list of person identifiers and when they were present here
	private Map<T, IPerson<T>> persons;
	
	private ILocation<T> location;

	//The previous hubs, that usually imply that a person had moved to this hub through the other
	private Collection<Point> previous;

	private IPersonListener<T> listener = (e)->{
		ILocation<T> check = e.getSnapshot();
		if(( !check.equals( this.location)) || check.isHealthy())
			return;
		location = createSnapShot();
		location = location.createWorst(check);
	};

	protected AbstractHub( ILocation<T> location) {
		super(location.getXpos(), location.getYpos());
		this.persons = new TreeMap<>();
		this.location = location;
		this.previous = new TreeSet<>();
	}

	@Override
	public ILocation<T> getLocation() {
		return location;
	}

	protected void setLocation(ILocation<T> location) {
		this.location = location;
	}

	public boolean isEmpty() {
		return this.persons.isEmpty();
	}
	
	protected void put(T step, IPerson<T> person ) {
		this.persons.put(step, person);
		person.addListener(listener);
	}

	@Override
	public Map<T, IPerson<T>> getPersons() {
		return persons;
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

}