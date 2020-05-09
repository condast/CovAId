package org.covaid.core.def;

import java.util.EventObject;

public class EnvironmentEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;
	
	private IPerson person;
	private int days;
	
	private IEnvironment.Events event;
	
	public EnvironmentEvent( IEnvironment<T> source, IPerson person, int days ) {
		this(source, days);
		this.person = person;
		event = IEnvironment.Events.ACTIVITY;
	}

	public EnvironmentEvent( IEnvironment<T> source, int days ) {
		this( source, IEnvironment.Events.NEW_DAY, days );
	}

	public EnvironmentEvent( IEnvironment<T> source, IFieldEnvironment.Events event, int days ) {
		super(source);
		this.days = days;
		this.event = event;
	}

	@SuppressWarnings("unchecked")
	public IEnvironment<T> getEnvironment() {
		return (IEnvironment<T>) super.getSource();
	}
	
	public IFieldEnvironment.Events getEvent() {
		return event;
	}

	public int getDays() {
		return days;
	}

	public IPerson getPerson() {
		return person;
	}
}
