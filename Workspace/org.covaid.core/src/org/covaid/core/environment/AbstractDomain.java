package org.covaid.core.environment;

import java.util.ArrayList;
import java.util.Collection;

import org.covaid.core.def.IDomainListener;

public abstract class AbstractDomain<T extends Object> implements IDomain<T>{

	public static final int DEFAULT_RADIUS = 10;//metres, the maximum movement that a person can make during one step in activity

	private String name;
	private int population;
	
	private IEnvironment<T> environment;
	
	private Collection<IDomainListener<T>> listeners;

	public AbstractDomain( String name ) {
		super();
		this.name = name;
		this.population = 1;
		this.listeners = new ArrayList<>();
	}
	
	public void init( int population ) {
		this.population = population;
	}
	
	@Override
	public String getName() {
		return name;
	}

	public int getPopulation() {
		return population;
	}

	@Override
	public IEnvironment<T> getEnvironment() {
		return environment;
	}
	
	public void setEnvironment(IEnvironment<T> environment) {
		this.environment = environment;
		this.population = 1;
	}

	protected boolean hasStarted() {
		return ( this.environment != null ) && this.environment.hasStarted();
	}

	@Override
	public void addListener( IDomainListener<T> listener ) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener( IDomainListener<T> listener ) {
		this.listeners.remove(listener);
	}

	protected void notifyListeners( DomainEvent<T> event ) {
		for( IDomainListener<T> listener: this.listeners )
			listener.notifyPersonChanged(event);
	}
}
