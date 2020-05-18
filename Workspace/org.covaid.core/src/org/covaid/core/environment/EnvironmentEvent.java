package org.covaid.core.environment;

import java.util.EventObject;

import org.covaid.core.def.IFieldEnvironment;
import org.covaid.core.def.IPerson;

public class EnvironmentEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;
	
	private IPerson<T> person;
	private int days;
	
	private IEnvironment.Events event;
	
	public EnvironmentEvent( IEnvironment<T> source, IPerson<T> person, int days ) {
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

	public IPerson<T> getPerson() {
		return person;
	}
}
