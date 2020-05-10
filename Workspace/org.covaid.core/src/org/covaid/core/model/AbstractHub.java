package org.covaid.core.model;

import java.util.Map;
import java.util.TreeMap;

import org.covaid.core.def.IHub;
import org.covaid.core.def.ILocation;
import org.covaid.core.def.IPerson;

public abstract class AbstractHub<T extends Object> extends Point implements IHub<T> {

	//A list of person identifiers and when they were present here
	private Map<T, IPerson<T>> persons;
	
	private ILocation<T> location;

	protected AbstractHub( ILocation<T> location) {
		super(location.getIdentifier(), location.getXpos(), location.getYpos());
		this.persons = new TreeMap<>();
		this.location = location;
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
	}

	@Override
	public Map<T, IPerson<T>> getPersons() {
		return persons;
	}
}