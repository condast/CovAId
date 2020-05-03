package org.covaid.core.environment;

import java.util.EventObject;

import org.covaid.core.def.IPerson;

public class DomainEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	
	private IPerson person;
	
	private AbstractDomain.DomainEvents event;
	
	public DomainEvent( AbstractDomain source, IPerson person ) {
		this(source);
		this.person = person;
		event = AbstractDomain.DomainEvents.UPDATE_PERSON;
	}

	public DomainEvent( AbstractDomain source ) {
		this( source, AbstractDomain.DomainEvents.UPDATE );
	}

	public DomainEvent(AbstractDomain source, AbstractDomain.DomainEvents event ) {
		super(source);
		this.event = event;
	}

	public AbstractDomain getDomain() {
		return (AbstractDomain) super.getSource();
	}
	
	public AbstractDomain.DomainEvents getEvent() {
		return event;
	}

	public IPerson getPerson() {
		return person;
	}
}
