package org.covaid.core.def;

import java.util.EventObject;

import org.covaid.core.environment.Environment;

public class EnvironmentEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	
	private IPerson person;
	private int days;
	
	private IEnvironment.Events event;
	
	public EnvironmentEvent( IEnvironment source, IPerson person, int days ) {
		this(source, days);
		this.person = person;
		event = IEnvironment.Events.ACTIVITY;
	}

	public EnvironmentEvent( IEnvironment source, int days ) {
		this( source, IEnvironment.Events.NEW_DAY, days );
	}

	public EnvironmentEvent( IEnvironment source, IEnvironment.Events event, int days ) {
		super(source);
		this.days = days;
		this.event = event;
	}

	public IEnvironment getEnvironment() {
		return (Environment) super.getSource();
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
