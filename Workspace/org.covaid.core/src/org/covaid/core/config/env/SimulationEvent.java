package org.covaid.core.config.env;

import java.util.EventObject;

public class SimulationEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	
	private Person person;
	
	public SimulationEvent(Object source, Person person ) {
		super(source);
		this.person = person;
	}

	public Person getPerson() {
		return person;
	}
}
