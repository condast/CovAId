package org.covaid.core.def;

import org.covaid.core.environment.DomainEvent;

@FunctionalInterface
public interface IDomainListener<T extends Object> {

	public void notifyPersonChanged( DomainEvent<T> event );
}
