package org.covaid.core.def;

import org.covaid.core.environment.DomainEvent;

@FunctionalInterface
public interface IDomainListener {

	public void notifyPersonChanged( DomainEvent event );
}
