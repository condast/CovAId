package org.covaid.core.model;

import java.util.EventObject;

import org.covaid.core.def.IEnvironment;
import org.covaid.core.def.IPerson;

public class EnvironmentEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	
	private IPerson person;
	private int days;
	
	private IEnvironment.Events event;
	
	public EnvironmentEvent(Object source, IPerson person, int days ) {
		this(source, days);
		this.person = person;
		event = IEnvironment.Events.UPDATE_PERSON;
	}

	public EnvironmentEvent(Object source, int days ) {
		this( source, IEnvironment.Events.NEW_DAY, days );
	}

	public EnvironmentEvent(Object source, IEnvironment.Events event, int days ) {
		super(source);
		this.days = days;
		this.event = event;
	}

	public IEnvironment.Events getEvent() {
		return event;
	}

	public int getDays() {
		return days;
	}

	public IPerson getPerson() {
		return person;
	}
}
