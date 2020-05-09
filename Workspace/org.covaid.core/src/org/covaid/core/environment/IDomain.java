package org.covaid.core.environment;

import org.covaid.core.def.IEnvironment;
import org.covaid.core.def.IDomainListener;

public interface IDomain<T extends Object> {

	public enum DomainEvents{
		UPDATE_PERSON,
		PERSONS_MOVED,
		UPDATE;
	}

	String getName();

	IEnvironment<T> getEnvironment();

	void setEnvironment(IEnvironment<T> abstractEnvironment);

	void addListener(IDomainListener listener);

	void removeListener(IDomainListener listener);

	void clear();
	
	public void init( int population );
	
	public void movePerson( T timeStep );

	void update(T date);

}