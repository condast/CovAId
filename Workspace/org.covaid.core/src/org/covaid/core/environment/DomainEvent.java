package org.covaid.core.environment;

import java.util.EventObject;

import org.covaid.core.def.IPerson;

public class DomainEvent<T extends Object> extends EventObject {
	private static final long serialVersionUID = 1L;
	
	private IPerson<T> person;
	
	private AbstractDomain.DomainEvents event;
	
	public DomainEvent( IDomain<T> source, IPerson<T> person ) {
		this(source);
		this.person = person;
		event = IDomain.DomainEvents.UPDATE_PERSON;
	}

	public DomainEvent( IDomain<T> source ) {
		this( source, IDomain.DomainEvents.UPDATE );
	}

	public DomainEvent(IDomain<T> source, AbstractDomain.DomainEvents event ) {
		super(source);
		this.event = event;
	}

	@SuppressWarnings("unchecked")
	public IDomain<T> getDomain() {
		return (IDomain<T>) super.getSource();
	}
	
	public AbstractDomain.DomainEvents getEvent() {
		return event;
	}

	public IPerson<T> getPerson() {
		return person;
	}
}
