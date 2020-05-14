package org.covaid.core.def;

import java.util.EventObject;

public class PersonEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;

	private T timeStep;
	private ILocation<T> snapshot;
	
	public PersonEvent(IPerson<T> source, T timeStep, ILocation<T> snapshot ) {
		super(source);
		this.snapshot = snapshot;
		this.timeStep = timeStep;
	}
	
	@SuppressWarnings("unchecked")
	public IPerson<T> getPerson(){
		return (IPerson<T>) super.getSource();
	}

	public ILocation<T> getSnapshot() {
		return snapshot;
	}

	public T getMoment() {
		return timeStep;
	}
}
