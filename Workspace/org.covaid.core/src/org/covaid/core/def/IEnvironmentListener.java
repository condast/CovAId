package org.covaid.core.def;

import org.covaid.core.model.EnvironmentEvent;

@FunctionalInterface
public interface IEnvironmentListener {

	public void notifyPersonChanged( EnvironmentEvent event );
}
