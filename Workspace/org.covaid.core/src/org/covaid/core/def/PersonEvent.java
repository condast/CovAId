package org.covaid.core.def;

import java.util.EventObject;

public class PersonEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;

	private T current;
	private T moment;
	private ILocation<T> snapshot;
	
	private IContagion contagion;
	
	public PersonEvent(IPerson<T> source, T current, T moment, IContagion contagion, ILocation<T> snapshot ) {
		super(source);
		this.current = current;
		this.snapshot = snapshot;
		this.contagion = contagion;
		this.moment = moment;
	}
	
	@SuppressWarnings("unchecked")
	public IPerson<T> getPerson(){
		return (IPerson<T>) super.getSource();
	}

	public ILocation<T> getSnapshot() {
		return snapshot;
	}

	public T getCurrent() {
		return current;
	}

	public T getMoment() {
		return moment;
	}

	public IContagion getContagion() {
		return contagion;
	}
}
