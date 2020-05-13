package org.covaid.core.def;

import java.util.EventObject;

public class PersonEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;

	private ILocation<T> snapshot;
	
	public PersonEvent(IPerson<T> source, ILocation<T> snapshot ) {
		super(source);
		this.snapshot = snapshot;
	}
	
	@SuppressWarnings("unchecked")
	public IPerson<T> getPerson(){
		return (IPerson<T>) super.getSource();
	}

	public ILocation<T> getSnapshot() {
		return snapshot;
	}
}
